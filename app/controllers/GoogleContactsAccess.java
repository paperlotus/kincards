package controllers;

import java.io.IOException;
import java.net.URL;

import play.libs.oauth.OAuth.ConsumerKey;
import play.mvc.Controller;
import play.mvc.Result;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
/*import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
*/
import com.google.gdata.client.contacts.*;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleContactsAccess extends Controller {
	/**
	 * @param args
	 */
	public ContactsService authenticateId(String userid, String password){

		ContactsService contactService = null;
		try{
			contactService = new ContactsService("kincards-974");
			contactService.getRequestFactory().setHeader("User-Agent", "kincards-974");
			
			System.out.println("user/pass="+userid+" / "+password);
			contactService.setUserCredentials("338206457458-qarnovnlcjtapgb40cdudt6u2nh21mvu.apps.googleusercontent.com", "cHWFcqschI40pHdk_xVsZCLQ");
			System.out.println("Credence set?");
						//this.userId = userid;
		}catch(Exception e){
			System.out.println(e);
		}
		return contactService;

	}
	
	public static Result goog() throws ServiceException, IOException{
		GoogleContactsAccess googleContactsAccess = new GoogleContactsAccess();

		ContactsService contactSrv = googleContactsAccess.authenticateId("prashant@prashantyadav.com", "BeingS0cial");
		System.out.println("contactSrv is not null..."+contactSrv.toString());

		googleContactsAccess.printAllContacts(contactSrv);
		return TODO;
	}
 
	/* This method will print details of all the contacts available in that particular Google account. */
 
	public void printAllContacts(ContactsService myService)throws ServiceException, IOException {

		// Request the feed
		URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
		System.out.println("getting feed..."+myService.getServiceVersion());		

		ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);

		// Print the results
		System.out.println(resultFeed.getTitle().getPlainText());
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			ContactEntry entry = resultFeed.getEntries().get(i);
			if (entry.hasName()) {
				Name name = entry.getName();
				if (name.hasFullName()) {
					String fullNameToDisplay = name.getFullName().getValue();
					if (name.getFullName().hasYomi()) {
						fullNameToDisplay += " (" + name.getFullName().getYomi() + ")";
					}
					System.out.println("\t\t" + fullNameToDisplay);
				} else {
					System.out.println("\t\t (no full name found)");
				}

				if (name.hasNamePrefix()) {
					System.out.println("\t\t" + name.getNamePrefix().getValue());
				} else {
					System.out.println("\t\t (no name prefix found)");
				}
				if (name.hasGivenName()) {
					String givenNameToDisplay = name.getGivenName().getValue();
					if (name.getGivenName().hasYomi()) {
						givenNameToDisplay += " (" + name.getGivenName().getYomi() + ")";
					}
					System.out.println("\t\t" + givenNameToDisplay);
				} else {
					System.out.println("\t\t (no given name found)");
				}

				if (name.hasAdditionalName()) {
					String additionalNameToDisplay = name.getAdditionalName().getValue();
					if (name.getAdditionalName().hasYomi()) {
						additionalNameToDisplay += " (" + name.getAdditionalName().getYomi() + ")";
					}
					System.out.println("\t\t" + additionalNameToDisplay);
				} else {
					System.out.println("\t\t (no additional name found)");
				}

				if (name.hasFamilyName()) {
					String familyNameToDisplay = name.getFamilyName().getValue();
					if (name.getFamilyName().hasYomi()) {
						familyNameToDisplay += " (" + name.getFamilyName().getYomi() + ")";
					}
					System.out.println("\t\t" + familyNameToDisplay);
				} else {
					System.out.println("\t\t (no family name found)");
				}

				if (name.hasNameSuffix()) {
					System.out.println("\t\t" + name.getNameSuffix().getValue());
				} else {
					System.out.println("\t\t (no name suffix found)");
				}

			} else {
				System.out.println("\t (no name found)");
			}

			System.out.println("Email addresses:");

			for (Email email : entry.getEmailAddresses()) {

				System.out.print(" " + email.getAddress());
				if (email.getRel() != null) {
					System.out.print(" rel:" + email.getRel());
				}
				if (email.getLabel() != null) {
					System.out.print(" label:" + email.getLabel());
				}
				if (email.getPrimary()) {
					System.out.print(" (primary) ");
				}
				System.out.print("\n");

			}

			System.out.println("IM addresses:");
			for (Im im : entry.getImAddresses()) {

				System.out.print(" " + im.getAddress());
				if (im.getLabel() != null) {
					System.out.print(" label:" + im.getLabel());
				}
				if (im.getRel() != null) {
					System.out.print(" rel:" + im.getRel());
				}
				if (im.getProtocol() != null) {
					System.out.print(" protocol:" + im.getProtocol());
				}
				if (im.getPrimary()) {
					System.out.print(" (primary) ");
				}
				System.out.print("\n");

			}

			System.out.println("Groups:");
			for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
				String groupHref = group.getHref();
				System.out.println("  Id: " + groupHref);
			}

			System.out.println("Extended Properties:");
			for (ExtendedProperty property : entry.getExtendedProperties()) {

				if (property.getValue() != null) {
					System.out.println("  " + property.getName() + "(value) = " +
							property.getValue());
				} else if (property.getXmlBlob() != null) {
					System.out.println("  " + property.getName() + "(xmlBlob)= " +
							property.getXmlBlob().getBlob());
				}

			}

			Link photoLink = entry.getContactPhotoLink();
			String photoLinkHref = photoLink.getHref();
			System.out.println("Photo Link: " + photoLinkHref);

			if (photoLink.getEtag() != null) {
				System.out.println("Contact Photo's ETag: " + photoLink.getEtag());
			}

			System.out.println("Contact's ETag: " + entry.getEtag());
		}
	}

}
