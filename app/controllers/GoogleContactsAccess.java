package controllers;

import java.io.IOException;
import java.net.URL;

import play.mvc.Controller;
import play.mvc.Result;


/*import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
*/
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleContactsAccess extends Controller {
	/**
	 * @param args
	 */

	public static Result goog() {
		ContactsService myService = new ContactsService("kincards");
		try {
			myService.setUserCredentials("prashant@prashantyadav.com", "BeingS0cial");

			 printAllContacts(myService);

			//displayAllContacts(myService);

		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();

		} catch (ServiceException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		} catch (IOException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}
		return null;

	}
	
	public static Result testg(){
		System.out.println("Hello...");
		return ok();
	}

	public static void printAllContacts(ContactsService myService)

	throws ServiceException, IOException {

		// Request the feed

		URL feedUrl = new URL(

		"http://www.google.com/m8/feeds/contacts/pryadav84@gmail.com/full");

		ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);

		// Print the results

		System.out.println(resultFeed.getTitle().getPlainText());

		for (int i = 0; i < resultFeed.getEntries().size(); i++) {

			ContactEntry entry = resultFeed.getEntries().get(i);

			System.out.println("\t" + entry.getTitle().getPlainText());

			System.out.println("Email addresses:");

//			for (Email email : entry.getEmailAddresses()) {
//
//				System.out.print(" " + email.getAddress());
//
//				if (email.getRel() != null) {
//
//					System.out.print(" rel:" + email.getRel());
//
//				}
//
//				if (email.getLabel() != null) {
//
//					System.out.print(" label:" + email.getLabel());
//
//				}
//
//				if (email.getPrimary()) {
//
//					System.out.print(" (primary) ");
//
//				}
//
//				System.out.print("\n");
//
//			}

			System.out.println("IM addresses:");

//			for (Im im : entry.getImAddresses()) {
//
//				System.out.print(" " + im.getAddress());
//
//				if (im.getLabel() != null) {
//
//					System.out.print(" label:" + im.getLabel());
//
//				}
//
//				if (im.getRel() != null) {
//
//					System.out.print(" rel:" + im.getRel());
//
//				}
//
//				if (im.getProtocol() != null) {
//
//					System.out.print(" protocol:" + im.getProtocol());
//
//				}
//
//				if (im.getPrimary()) {
//
//					System.out.print(" (primary) ");
//
//				}
//
//				System.out.print("\n");
//
//			}

			System.out.println("Groups:");

//			for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
//
//				String groupHref = group.getHref();
//
//				System.out.println(" Id: " + groupHref);
//
//			}

			System.out.println("Extended Properties:");

//			for (ExtendedProperty property : entry.getExtendedProperties()) {
//
//				if (property.getValue() != null) {
//
//					System.out.println(" " + property.getName() + "(value) = "
//
//					+ property.getValue());
//
//				} else if (property.getXmlBlob() != null) {
//
//					System.out.println(" " + property.getName()
//
//					+ "(xmlBlob)= " + property.getXmlBlob().getBlob());
//
//				}
//
//			}

			String photoLink = entry.getContactPhotoLink().getHref();

			System.out.println("Photo Link: " + photoLink);
			System.out.println("Contact's ETag: " + entry.getEtag());

		}

	}

	

}
