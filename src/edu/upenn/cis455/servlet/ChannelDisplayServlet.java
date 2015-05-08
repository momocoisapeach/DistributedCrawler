package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.RawFile;

// TODO: Auto-generated Javadoc
/**
 * Servlet implementation class ChannelDisplayServlet.
 */
public class ChannelDisplayServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Do get.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String chnl = request.getParameter("channel");
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		PrintWriter pw = response.getWriter();
		
		if(chnl!=null && db.hasChannel(chnl)){
			String xslt = db.getXslt(chnl);
			ArrayList<String> docUrls = db.getMatchedUrls(chnl);
			ArrayList<String> docs = new ArrayList<String>();
			ArrayList<String> lstCrawled = new ArrayList<String>();
			ArrayList<String> processing = new ArrayList<String>();
			
			for(int i = 0; i <docUrls.size(); i++){
				String file = db.getFile(docUrls.get(i));
				if(file!=null && file.startsWith("<?xml")){
					processing.add(file.substring(0,file.indexOf(">")+1));
					file = file.substring(file.indexOf(">")+1);
				}
	//			System.out.println(file);
				docs.add(file);
				Date temp = db.getLstCrawled(docUrls.get(i));
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
			    String lst = sdf.format(temp);
				lstCrawled.add(lst);
			}
			response.setContentType("text/xml");
			if(chnl.equals("RSS aggregator1")){
				StringBuilder body = new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
						+"<?xml-stylesheet type=\"text/xsl\" href=\"/HW2/files/rss.xsl\"?>"
		//				+ xslt
		//				+ "\"?>"
						+ "<rsscollection>");
				
				for(int i = 0; i < docUrls.size(); i++){
					body.append("<document" +" crawled=\""+lstCrawled.get(i)+"\" location=\""+docUrls.get(i)+"\">");
					body.append(docs.get(i));	
					body.append("</document>");
				}
				body.append("</rsscollection>");
				pw.println(body);
				pw.flush();
			}
			else{
				StringBuilder body = new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
						+"<?xml-stylesheet type=\"text/xsl\" href=\"/HW2/files/default.xsl\"?>"
		//				+ xslt
		//				+ "\"?>"
						+ "<documentcollection>");
				for(int i = 0; i < docUrls.size(); i++){
					body.append("<document" +" crawled=\""+lstCrawled.get(i)+"\" location=\""+docUrls.get(i)+"\">");
					body.append(docs.get(i));	
					body.append("</document>");
				}
				body.append("</documentcollection>");
				System.out.println(body);
				
				pw.println(body);
				pw.flush();
			}
			
			
	//		response.setContentType("text/xml");
	//		String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<note><to>Tove</to></note>";
	//		PrintWriter pw = response.getWriter();
	//		pw.println(body);
	//		pw.flush();
			
		}
		else{
			
            pw.println("<font color=red>No Such Channel!</font><br><br>");
            pw.println("<form action=\"/HW2/Login\"><input type=\"submit\" value=\"Log in\"></form>");
            pw.println("<form action=\"/HW2/Signup\"><input type=\"submit\" value=\"Sign up\"></form>");
		}
		db.closeEnv();
	}

	/**
	 * Do post.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
