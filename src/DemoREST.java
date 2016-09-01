import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
/**
 * Servlet implementation class DemoREST
 */
@WebServlet("/DemoREST")
public class DemoREST extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String INSTANCE_URL = "INSTANCE_URL";

	private void showAccounts(String instanceUrl, String accessToken,
			PrintWriter writer) throws ServletException, IOException {
		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(instanceUrl
				+ "/services/data/v20.0/query");

		// set the token in the header
		get.setRequestHeader("Authorization", "OAuth " + accessToken);

		// set the SOQL as a query param
		NameValuePair[] params = new NameValuePair[1];

		params[0] = new NameValuePair("q",
				"SELECT Name, Id from Account LIMIT 100");
		get.setQueryString(params);

		try {
			httpclient.executeMethod(get);
			if (get.getStatusCode() == HttpStatus.SC_OK) {
				// Now lets use the standard java json classes to work with the
				// results
				try {
					JSONObject response = new JSONObject(
							new JSONTokener(new InputStreamReader(
									get.getResponseBodyAsStream())));
					System.out.println("Query response: "
							+ response.toString(2));
					
					writer.println("<html>");
					writer.println("<body>");
					writer.println("<t1>" + "<h1>"+(response.getInt("totalSize"))+ " record(s) returned"+"</h1>"+ "</t1>");
					writer.println("</body>");
					writer.println("</html>");
					//writer.write(response.getInt("totalSize")
							//+ " record(s) returned\n\n");
					
					JSONArray results = response.getJSONArray("records");
					
					writer.println("<html>");
					writer.println("<body>");
					writer.println("<table >");
					writer.println("<tr>");
					writer.println("<td><b>"+ "Name"+  "</td>");
					writer.println("<td><b>"+ "&nbsp;&nbsp;&nbsp;&nbsp;"+  "</td>");
					writer.println("<td><b>" +"Id"+  "</td>");
					writer.println("</tr>");
					
					for (int i = 0; i < results.length(); i++) {
						
						
						writer.println("<tr>");
						
						writer.println("<td>" +results.getJSONObject(i).getString("Name")+  "</td>");
						writer.println("<td>"+ "&nbsp;&nbsp;&nbsp;&nbsp;"+  "</td>");
						writer.println("<td>" +results.getJSONObject(i).getString("Id")+  "</td>");
						
						writer.println("</tr>");
						
						
						//writer.write("\n"+results.getJSONObject(i).getString("Id")
							//	+ ", "
							//	+ results.getJSONObject(i).getString("Name")
							//	+ "\n");
					}
					writer.println("</table >");
					
					writer.println("</body>");
					writer.println("</html>");
					
					
				} catch (JSONException e) {
					e.printStackTrace();
					throw new ServletException(e);
				}
			}
		} finally {
			get.releaseConnection();
		}
		
	}

	private String createAccount(String name, String instanceUrl,
			String accessToken, PrintWriter writer) throws ServletException,
			IOException {
		String accountId = null;

		HttpClient httpclient = new HttpClient();

		JSONObject account = new JSONObject();

		try {
			account.put("Name", name);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

		PostMethod post = new PostMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/");

		post.setRequestHeader("Authorization", "OAuth " + accessToken);
		post.setRequestEntity(new StringRequestEntity(account.toString(),
				"application/json", null));

		try {
			httpclient.executeMethod(post);
			writer.println("<html>");
			writer.println("<body>");
			
			writer.println("<br><t1>" +"HTTP status " + post.getStatusCode()	+ ", creating account... "+"</t1><br><br>");
			
			//writer.write("\n"+"HTTP status " + post.getStatusCode()
				//	+ " creating account\n\n");

			if (post.getStatusCode() == HttpStatus.SC_CREATED) {
				try {
					JSONObject response = new JSONObject(new JSONTokener(
							new InputStreamReader(
									post.getResponseBodyAsStream())));
					System.out.println("Create response: "
							+ response.toString(2));

					if (response.getBoolean("success")) {
						
						accountId = response.getString("id");
						writer.println("<t1>" +"New record id " + accountId +"</t1><br><br>");
						//writer.write("\n"+"New record id " + accountId + "\n\n");
					}
				} catch (JSONException e) {
					e.printStackTrace();
					//throw new ServletException(e);
				}
			}
			writer.println("</body>");
			writer.println("</html>");
		} finally {
			post.releaseConnection();
		}

		return accountId;
	}

	private void showAccount(String accountId, String instanceUrl,
			String accessToken, PrintWriter writer) throws ServletException,
			IOException {
		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/" + accountId);

		// set the token in the header
		get.setRequestHeader("Authorization", "OAuth " + accessToken);

		try {
			httpclient.executeMethod(get);
			if (get.getStatusCode() == HttpStatus.SC_OK) {
				try {
					JSONObject response = new JSONObject(
							new JSONTokener(new InputStreamReader(
									get.getResponseBodyAsStream())));
					System.out.println("Query response: "
							+ response.toString(2)
							)
							;
					writer.println("<html>");
					writer.println("<body>");
					writer.println("<t1><br><u>" +"Account content"+"</u></t1><br>");
					writer.println("<table >");
					
					//writer.write("\n"+"Account content\n\n");
					response.remove("attributes");
					Iterator iterator = response.keys();
					while (iterator.hasNext()) {
						
						String key = (String) iterator.next();
						
						String value = null;
						
						if(!response.isNull(key)){
							try{
							value = response.getString(key);
							}catch(Exception e)
							{
								boolean b=response.getBoolean(key);
								value = String.valueOf(b);
								
							}
						} else {
							value = null;
						}
						writer.println("<tr>");
						writer.println("<td>" +key + ":" +  "</td>");
						writer.println("<td>"+ "&nbsp;&nbsp;&nbsp;&nbsp;"+  "</td>");
						writer.println("<td>" + (value!= null? value: "")+  "</td>");
						//writer.write("\n"+key + ":" + (value!= null? value: "")
								//+ "\n");
						writer.println("</tr>");
					}
					writer.println("</table >");
					writer.println("</body>");
					writer.println("</html>");
				} catch (JSONException e) {
					e.printStackTrace();
					throw new ServletException(e);
				}
			}
		} finally {
			get.releaseConnection();
		}
	}

	private void updateAccount(String accountId, String newName, String city,
			String instanceUrl, String accessToken, PrintWriter writer)
			throws ServletException, IOException {
		HttpClient httpclient = new HttpClient();

		JSONObject update = new JSONObject();

		try {
			update.put("Name", newName);
			update.put("BillingCity", city);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

		PostMethod patch = new PostMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/" + accountId) {
			@Override
			public String getName() {
				return "PATCH";
			}
		};

		patch.setRequestHeader("Authorization", "OAuth " + accessToken);
		patch.setRequestEntity(new StringRequestEntity(update.toString(),
				"application/json", null));

		try {
			httpclient.executeMethod(patch);
			writer.println("<html>");
			writer.println("<body>");
			
			writer.println("<br><t1>" +"HTTP status " + patch.getStatusCode()	+ " updating account " + accountId+"</t1><br><br>");
			
			writer.println("/<html>");
			writer.println("/<body>");
			
			//writer.write("\n"+"HTTP status " + patch.getStatusCode()
			//		+ " updating account " + accountId + "\n\n");
		} finally {
			patch.releaseConnection();
		}
	}

	private void deleteAccount(String accountId, String instanceUrl,
			String accessToken, PrintWriter writer) throws IOException {
		HttpClient httpclient = new HttpClient();

		DeleteMethod delete = new DeleteMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/" + accountId);

		delete.setRequestHeader("Authorization", "OAuth " + accessToken);

		try {
			httpclient.executeMethod(delete);
			writer.println("<html>");
			writer.println("<body>");
			
			writer.println("<br><t1>" +"HTTP status " + delete.getStatusCode()	+ " deleting account " + accountId+"</t1><br><br>");
			
			writer.println("/<html>");
			writer.println("/<body>");
			//writer.write("\n"+"HTTP status " + delete.getStatusCode()
			//		+ " deleting account " + accountId + "\n\n");
		} finally {
			delete.releaseConnection();
		}
	}
  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DemoREST() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter writer = response.getWriter();
		String accessToken = request.getParameter("access");
		String instanceUrl = request.getParameter("url");
		String choice = request.getParameter("choice");
		String txName = request.getParameter("txName");
		String txCity = request.getParameter("txCity");
		String txAccount = request.getParameter("txAccount");
		//String accessToken = (String) request.getSession().getAttribute(
		//		ACCESS_TOKEN);

		//String instanceUrl = (String) request.getSession().getAttribute(
		//		INSTANCE_URL);
		System.out.println(txName);
		if (accessToken == null) {
			writer.println("<html>");
			writer.println("<body>");
			writer.println("<t1>" + "<h2>Error - no access token</h2>"+ "</t1>");
			writer.println("</body>");
			writer.println("</html>");
			// writer.write("Error - no access token");
			return;
		}
		writer.println("<html>");
		writer.println("<body>");

		writer.println("<table >");
		writer.println("<tr>");
		writer.println(" <td><b>We have an access token:</b> </td>");
		writer.println("</tr>");
		writer.println("<tr>");
		writer.println("<td >" + accessToken + "</td>");

		writer.println("</tr>");

		writer.println(" <tr>");

		writer.println(" <td><b1>Using instance </b></td>");
		writer.println("</tr>");
		writer.println("<tr>");
		writer.println("<td>" + instanceUrl + "</td>");
		writer.println("</tr>");

		writer.println("</table>");
		writer.println("</body>");
		writer.println("</html>");
		
		//writer.write("We have an access token: " + accessToken + "\n"
			//	+ "Using instance " + instanceUrl + "\n\n");
		
		if(choice.equalsIgnoreCase("show") )
		{
			System.out.println(choice);
		showAccounts(instanceUrl, accessToken, writer);
		
		}
		
		
		else if(choice.equalsIgnoreCase("create"))
		
		{
		String accountId = createAccount(txName, instanceUrl,
						accessToken, writer);
				
				showAccount(accountId, instanceUrl, accessToken, writer);
		}
		else if(choice.equalsIgnoreCase("update"))
			
		{
			updateAccount(txAccount, txName, txCity,
					instanceUrl, accessToken, writer);
			
				
			showAccount(txAccount, instanceUrl, accessToken, writer);
		}
		else if(choice.equalsIgnoreCase("delete"))
			
		{
			deleteAccount(txAccount, instanceUrl, accessToken, writer);
				
			showAccounts( instanceUrl, accessToken, writer);
		}
		
		//showAccounts(instanceUrl, accessToken, writer);
		
		//updateAccount(accountId, "My New Org, Inc", "San Francisco",
		//		instanceUrl, accessToken, writer);
		
		//showAccount(accountId, instanceUrl, accessToken, writer);
		
		//deleteAccount(accountId, instanceUrl, accessToken, writer);
		
		//showAccounts(instanceUrl, accessToken, writer);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
