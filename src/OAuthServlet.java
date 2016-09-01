

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Servlet implementation class OAuthServlet
 */
@WebServlet(name = "oauth", urlPatterns = { "/oauth/*", "/oauth" }, initParams = {
		// clientId is 'Consumer Key' in the Remote Access UI
		//@WebInitParam(name = "clientId", value = "3MVG9Km_cBLhsuPzTtcGHsZpj9JylyezngYKNi.dNkSQmA0fAdwMD9OzkQEPFDJv1UgVF5tcERKtuiP5Yiin3"),
		// clientSecret is 'Consumer Secret' in the Remote Access UI
		//@WebInitParam(name = "clientSecret", value = "6135262856068035680"),
		// This must be identical to 'Callback URL' in the Remote Access UI
		//@WebInitParam(name = "redirectUri", value = "https://localhost:8443/RestTest/oauth/_callback"),
		@WebInitParam(name = "environment", value = "https://login.salesforce.com"), })
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String INSTANCE_URL = "INSTANCE_URL";
	private static final String _URL = "_URL";
	private String clientId = null;
	private String clientSecret = null;
	private String redirectUri = null;
	private String environment = null;
	private String authUrl = null;
	private String tokenUrl = null;
	String instanceUrl = null;   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OAuthServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		clientId = "3MVG9ZL0ppGP5UrBfg7lLR.1nXBH0.3HKph0lL1REL10qOhu1eOxrNgvSaPGPt2ANaTqtr33PD95_S1kuOh8s";//this.getInitParameter("clientId");
		clientSecret = "3255284275787900504";//this.getInitParameter("clientSecret");
		redirectUri = "https://powerful-plains-50930.herokuapp.com//oauth/_callback";//this.getInitParameter("redirectUri");
		environment = "https://login.salesforce.com";//this.getInitParameter("environment");

		try {
			authUrl = environment
					+ "/services/oauth2/authorize?response_type=code&client_id="
					+ clientId + "&redirect_uri="
					+ URLEncoder.encode(redirectUri, "UTF-8");
			System.out.println("try");
		} catch (UnsupportedEncodingException e) {
			throw new ServletException(e);
		}

		tokenUrl = environment + "/services/oauth2/token";
		System.out.println("tokenUrl");
		System.out.println(tokenUrl);
	}

	/**
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String accessToken = (String) request.getSession().getAttribute(
				ACCESS_TOKEN);
		System.out.println(accessToken);
		System.out.println("Im in");
		if (accessToken == null) {
			
			if (request.getRequestURI().endsWith("oauth")) {
				System.out.println("inside oauth");
				// we need to send the user to authorize
				response.sendRedirect(authUrl);
				System.out.println("out oauth");
				return;
			}
			else {
				System.out.println("Auth successful - got callback");

				String code = request.getParameter("code");
				System.out.println(code);
				
				HttpClient httpclient = new HttpClient();
				
				PostMethod post = new PostMethod(tokenUrl);
				
				post.addParameter("code", code);
				post.addParameter("grant_type", "authorization_code");
				post.addParameter("client_id", clientId);
				post.addParameter("client_secret", clientSecret);
				post.addParameter("redirect_uri", redirectUri);
				
				try {
					httpclient.executeMethod(post);

					try {
						JSONObject authResponse = new JSONObject(
								new JSONTokener(new InputStreamReader(
										post.getResponseBodyAsStream())));
						System.out.println("Auth response: "
								+ authResponse.toString(2));

						accessToken = authResponse.getString("access_token");
						instanceUrl = authResponse.getString("instance_url");

						System.out.println("Got access token: " + accessToken);
					} catch (JSONException e) {
						e.printStackTrace();
						throw new ServletException(e);
					}
				} finally {
					post.releaseConnection();
				}
				
			}
			// Set a session attribute so that other servlets can get the access
			// token
			request.getSession().setAttribute(ACCESS_TOKEN, accessToken);

			// We also get the instance URL from the OAuth response, so set it
			// in the session too
			request.getSession().setAttribute(INSTANCE_URL, instanceUrl);
			
		}
		System.out.println(instanceUrl);
		request.getSession().setAttribute(_URL, request.getContextPath() + "/DemoREST");
		
		response.sendRedirect("https://powerful-plains-50930.herokuapp.com/Accessed.jsp?accesstocken="+accessToken+"&instaUrl="+instanceUrl);
		//response.sendRedirect(request.getContextPath() + "/DemoREST");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");  
		PrintWriter out = response.getWriter();  
		out.print("You are successfully registered...");  
	}

}
