package com.kadir.twitterbots.elephant.importer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kadir.twitterbots.authentication.BotAuthenticator;
import com.kadir.twitterbots.elephant.util.ElephantConstants;
import com.kadir.twitterbots.elephant.util.ElephantUtil;
import com.kadir.twitterbots.exceptions.PropertyNotLoadedException;
import com.kadir.twitterbots.ratelimithandler.handler.RateLimitHandler;
import com.kadir.twitterbots.ratelimithandler.process.ApiProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author akadir
 * Date: 16/01/2019
 * Time: 19:50
 */
public class Importer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Twitter twitter;
    private String listOfImportListsFileName;

    public void run() {
        try {
            logger.info("import starting");
            readProperties();
            authenticate();
            importLists();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void authenticate() {
        twitter = BotAuthenticator.authenticate(ElephantConstants.PROPERTIES_FILE_NAME, ElephantConstants.API_KEYS_PREFIX);
    }

    private void importLists() throws IOException {
        String backedUpLists = loadImportFileContent();
        JsonArray backedUpListsJsonArray = convertFileContentToJsonArray(backedUpLists);
        createLists(backedUpListsJsonArray);
    }

    private void createLists(JsonArray listsJsonArray) {
        for (int i = listsJsonArray.size() - 1; i > -1; i--) {
            JsonObject list = (JsonObject) listsJsonArray.get(i);
            createList(list);
        }
        logger.info("lists are created");
    }

    private void createList(JsonObject list) {
        try {
            String name = list.get("name").getAsString();
            String description = list.get("description").getAsString();
            JsonArray members = list.getAsJsonArray("members");
            long[] memberIdArray = getAppropriateUserIdArray(members);

            if (memberIdArray.length > 0) {
                UserList newList = twitter.createUserList(name, false, description);
                RateLimitHandler.handle(twitter.getId(), newList.getRateLimitStatus(), ApiProcessType.CREATE_USER_LIST);

                List<long[]> dividedIDs = ElephantUtil.divideArray(memberIdArray, 100);

                for (long[] ids : dividedIDs) {
                    newList = twitter.createUserListMembers(newList.getId(), ids);
                    RateLimitHandler.handle(twitter.getId(), newList.getRateLimitStatus(), ApiProcessType.CREATE_USER_LIST_MEMBERS);
                }

                logger.info("list created name: {} - member count: {}", newList.getName(), newList.getMemberCount());
            } else {
                logger.info("list has no appropriate user as member to create {}", name);
            }

        } catch (TwitterException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("failed to handle rate limit", e);
        }
    }

    private long[] getAppropriateUserIdArray(JsonArray members) {
        List<Long> appropriateUserIdList = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = (JsonObject) members.get(i);
            long userId = member.get("id").getAsLong();
            try {
                User user = twitter.showUser(userId);
                RateLimitHandler.handle(twitter.getId(), user.getRateLimitStatus(), ApiProcessType.SHOW_USER);
                if (!user.isProtected()) {
                    appropriateUserIdList.add(user.getId());
                } else {
                    Relationship relationship = twitter.showFriendship(twitter.getId(), user.getId());
                    RateLimitHandler.handle(twitter.getId(), relationship.getRateLimitStatus(), ApiProcessType.SHOW_FRIENDSHIP);

                    if (relationship.isSourceFollowingTarget()) {
                        appropriateUserIdList.add(user.getId());
                    }
                }
            } catch (TwitterException e) {
                logger.error(e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("failed to handle rate limit", e);
            }
        }

        return appropriateUserIdList.stream().mapToLong(x -> x).toArray();
    }

    private JsonArray convertFileContentToJsonArray(String fileContent) {
        JsonParser parser = new JsonParser();
        JsonArray backedUpListsJsonArray = parser.parse(fileContent).getAsJsonArray();
        logger.info("import file content is parsed successfully");
        return backedUpListsJsonArray;
    }

    private String loadImportFileContent() throws IOException {
        Path path = Paths.get(listOfImportListsFileName);
        String fileContent;

        fileContent = new String(Files.readAllBytes(path));
        logger.info("import file content is loaded successfully");
        return fileContent;
    }

    private void readProperties() {
        Properties properties = new Properties();

        File propertyFile = new File(ElephantConstants.PROPERTIES_FILE_NAME);

        try (InputStream input = new FileInputStream(propertyFile)) {
            properties.load(input);
            listOfImportListsFileName = properties.getProperty("import-file");
            logger.info("set import-file:{}", listOfImportListsFileName);
            logger.info("All properties loaded from file: {}", ElephantConstants.PROPERTIES_FILE_NAME);
        } catch (IOException e) {
            logger.error("error occurred while getting properties from file  {} ", ElephantConstants.PROPERTIES_FILE_NAME, e);
            throw new PropertyNotLoadedException(ElephantConstants.PROPERTIES_FILE_NAME);
        }
    }
}
