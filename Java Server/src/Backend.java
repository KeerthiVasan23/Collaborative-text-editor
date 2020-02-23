

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Servlet implementation class Backend
 */
@WebServlet("/files")
public class Backend extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	Server server = new Server(4444);

    /**
     * Default constructor. 
     */
    public Backend() {
        // TODO Auto-generated constructor stub
    }

    
    static String filePath="/Users/test/Documents/";
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String fileName;
		
		if(request.getParameter("fileName")!=null)
		{
			fileName=request.getParameter("fileName");
			File file = new File(filePath+fileName);
			if (!file.isFile())
				file.createNewFile();
			
			String fileContents = new String(Files.readAllBytes(Paths.get(filePath+fileName)));
			System.out.println("Sent:  "+fileContents);
			response.getWriter().write(fileContents);
		}
		
		else
		{
			File dir = new File(filePath);
			String[] fileList = dir.list();
			String fileNames;
			StringBuilder sb = new StringBuilder(100);
	        System.out.println(sb.toString());
			for(String name:fileList){
				sb.append(name).append(",");
			}
			fileNames=sb.toString();
			response.getWriter().write(fileNames);
		}
			 
        
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			String fileName = request.getParameter("filename");
			String data = request.getParameter("edited");
			String text = request.getParameter("text");
			String start=request.getParameter("start"); 
			String end=request.getParameter("end");
			String ip=request.getParameter("ip");
			System.out.println("Received: "+data);
			Files.write(Paths.get(filePath+fileName), data.getBytes());
			server.sendToAll(fileName+"\n"+start+"\n"+end+"\n"+data,ip);
		}
	    catch(Exception e) {}
	}
	
}





