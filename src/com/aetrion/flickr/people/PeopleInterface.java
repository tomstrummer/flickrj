package com.aetrion.flickr.people;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aetrion.flickr.Authentication;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RESTResponse;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.contacts.OnlineStatus;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.util.XMLUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Interface for finding Flickr users.
 *
 * @author Anthony Eden
 */
public class PeopleInterface {

    public static final String METHOD_FIND_BY_EMAIL = "flickr.people.findByEmail";
    public static final String METHOD_FIND_BY_USERNAME = "flickr.people.findByUsername";
    public static final String METHOD_GET_INFO = "flickr.people.getInfo";
    public static final String METHOD_GET_ONLINE_LIST = "flickr.people.getOnlineList";
    public static final String METHOD_GET_PUBLIC_PHOTOS = "flickr.people.getPublicPhotos";

    private String apiKey;
    private REST restInterface;

    public PeopleInterface(String apiKey, REST restInterface) {
        this.apiKey = apiKey;
        this.restInterface = restInterface;
    }

    /**
     * Find the user by their email address.
     *
     * @param email The email address
     * @return The User
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public User findByEmail(String email) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_FIND_BY_EMAIL));
        parameters.add(new Parameter("api_key", apiKey));

        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }

        parameters.add(new Parameter("find_email", email));

        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } else {
            Element userElement = (Element) response.getPayload();
            User user = new User();
            user.setId(userElement.getAttribute("nsid"));
            user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
            return user;
        }
    }

    /**
     * Find a User by the username.
     *
     * @param username The username
     * @return The User object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public User findByUsername(String username) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_FIND_BY_USERNAME));
        parameters.add(new Parameter("api_key", apiKey));

        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }

        parameters.add(new Parameter("username", username));

        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } else {
            Element userElement = (Element) response.getPayload();
            User user = new User();
            user.setId(userElement.getAttribute("nsid"));
            user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
            return user;
        }
    }

    /**
     * Get info about the specified user.
     *
     * @param userId The user ID
     * @return The User object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public User getInfo(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_INFO));
        parameters.add(new Parameter("api_key", apiKey));

        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }

        parameters.add(new Parameter("user_id", userId));

        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } else {
            Element userElement = (Element) response.getPayload();
            User user = new User();
            user.setId(userElement.getAttribute("nsid"));
            user.setAdmin("1".equals(userElement.getAttribute("isadmin")));
            user.setPro("1".equals(userElement.getAttribute("ispro")));
            user.setIconServer(userElement.getAttribute("iconserver"));
            user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
            user.setRealname(XMLUtilities.getChildValue(userElement, "realname"));
            user.setLocation(XMLUtilities.getChildValue(userElement, "location"));

            Element photosElement = XMLUtilities.getChild(userElement, "photos");
            user.setPhotosFirstDate(XMLUtilities.getChildValue(photosElement, "firstdate"));
            user.setPhotosFirstDateTaken(XMLUtilities.getChildValue(photosElement, "firstdatetaken"));
            user.setPhotosCount(XMLUtilities.getChildValue(photosElement, "count"));

            return user;
        }
    }

    /**
     * Get the list of current online users.
     *
     * @return The list of online users
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getOnlineList() throws IOException, SAXException, FlickrException {
        List online = new ArrayList();

        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_ONLINE_LIST));
        parameters.add(new Parameter("api_key", apiKey));

        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }

        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } else {
            Element onlineElement = (Element) response.getPayload();
            NodeList userNodes = onlineElement.getElementsByTagName("user");
            for (int i = 0; i < userNodes.getLength(); i++) {
                Element userElement = (Element) userNodes.item(i);
                User user = new User();
                user.setId(userElement.getAttribute("nsid"));
                user.setUsername(userElement.getAttribute("username"));
                user.setOnline(OnlineStatus.fromType(userElement.getAttribute("online")));
                if (user.getOnline() == OnlineStatus.AWAY) {
                    Text awayMessageElement = (Text) userElement.getFirstChild();
                    if (awayMessageElement != null)
                        user.setAwayMessage(awayMessageElement.getData());
                }
                online.add(user);
            }
            return online;
        }
    }


    /**
     * Get a collection of public photos for the specified user ID.
     *
     * @param userId The User ID
     * @param perPage The number of photos per page
     * @param page The page offset
     * @return The collection of Photo objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getPublicPhotos(String userId, int perPage, int page) throws IOException, SAXException,
            FlickrException {
        List photos = new ArrayList();

        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_PUBLIC_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }

        parameters.add(new Parameter("user_id", userId));

        if (perPage > 0) {
            parameters.add(new Parameter("per_page", new Integer(perPage)));
        }
        if (page > 0) {
            parameters.add(new Parameter("page", new Integer(page)));
        }

        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } else {
            Element photosElement = (Element) response.getPayload();
            NodeList photoNodes = photosElement.getElementsByTagName("photo");
            for (int i = 0; i < photoNodes.getLength(); i++) {
                Element photoElement = (Element) photoNodes.item(i);
                Photo photo = new Photo();
                photo.setId(photoElement.getAttribute("id"));

                User owner = new User();
                owner.setId(photoElement.getAttribute("owner"));
                photo.setOwner(owner);

                photo.setSecret(photoElement.getAttribute("secret"));
                photo.setServer(photoElement.getAttribute("server"));
                photo.setTitle(photoElement.getAttribute("name"));
                photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
                photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
                photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));

                photos.add(photo);
            }
            return photos;
        }
    }

}
