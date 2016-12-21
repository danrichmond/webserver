import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer
{
	public static void main(String argv[]) throws Exception
	{
		// Set port number
		int portNum = 8000;

		// Establish the listen socket.
		ServerSocket serverSocket = new ServerSocket(portNum);

		// Infinite loop
		for(;;) {
			// Create a new socket
			Socket socket = serverSocket.accept();

			// Create the HTTP request 
			HttpRequest httpRequest = new HttpRequest(socket);

			// Create and start a new thread
			Thread thread = new Thread(httpRequest);
			thread.start();
		}
	}
}

class HttpRequest implements Runnable
{
	Socket socket;
	String CRLF = "\r\n";

	// Constructor for HTTPRequest
	public HttpRequest(Socket socket) throws Exception
	{
		this.socket = socket;
	}

	/**
	 * Method to run. Tries to call processRequest
	 * catches an exception
	 */
	public void run()
	{
		try {
			processRequest();
		} catch(Exception e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 * Processes the request
	 * 
	 * @throws Exception
	 */
	private void processRequest() throws Exception
	{
		// Create input and output streams
		InputStream instream = socket.getInputStream();
		DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

		// Create input stream reader
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

		// Read in the request line
		String requestLine = reader.readLine();

		// Print request
		System.out.println();
		System.out.println(requestLine);

		// Tokenize the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken();
		String fileName = tokens.nextToken();

		// Concatenate a period so the file
		// can be found throughout all directories
		fileName = "." + fileName;

		// Open the file
		FileInputStream fileInStream = null;
		boolean fileIsThere = true;
		try {
			fileInStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileIsThere = false;
		}

		// Create response message
		String status = "";
		String fileType = "";
		String body = "";

		if (fileIsThere) {
			status = "HTTP/1.0 200 ";
			fileType = "Content-type: " + "" + fileType(fileName) + CRLF;
		} else {
			status = "404 Not Found " + CRLF;
		}

		//	Send info
		outStream.writeBytes(status);
		outStream.writeBytes(fileType);
		outStream.writeBytes(CRLF);

		// Send body
		if (fileIsThere) {
			sendBytes(fileInStream, outStream);
			fileInStream.close();
		} else {
			outStream.writeBytes(body);
		}
		outStream.writeBytes(status); 
		outStream.writeBytes(fileType); 

		// Print headerLine
		String header = null;
		while ((header = reader.readLine()).length() != 0) {
			System.out.println(header);
		}
		outStream.close();
		reader.close();
		socket.close();
	}
	
	/**
	 * Method to find the type of the file
	 * 
	 * @param fileName - file to find the type of
	 * @return type of file
	 */
	private static String fileType(String fileName)
	{
		if(fileName.endsWith(".html") || fileName.endsWith(".htm")) {
			return "text/html";
		}
		if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if(fileName.endsWith(".gif")) {
			return "image/gif";
		}
		return "application/octet-stream";
	}

	/**
	 * Method to prepare the streams
	 * 
	 * @param fileInStream
	 * @param outStream
	 * @throws Exception
	 */
	private static void sendBytes(FileInputStream fileInStream, OutputStream outStream) throws Exception
	{
		// Create buffer for output stream
		byte[] buffer = new byte[1024];
		int bytes = 0;

		// Copy file to outStream
		while((bytes = fileInStream.read(buffer)) != -1 )
		{
			outStream.write(buffer, 0, bytes);
		}
	}
}