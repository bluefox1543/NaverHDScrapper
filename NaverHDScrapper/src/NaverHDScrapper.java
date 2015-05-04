import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class NaverHDScrapper {

	public static void main(String[] args) {
		new HDScrapFrame();
	}

	private static class HDScrapFrame extends JFrame{
		JTextField urlinput,postinput;
		JLabel urlLabel,postLabel,resultLabel,infoLabel,linkLabel,resultField,resultLabel2,resultField2;
		JPanel inputLine,postLine,setupLine,infoPanel,resultPanel;
		JButton inputbtn;
		String userAgent = "Mozilla/4.0",videoURLString,videoURLString2;

		public HDScrapFrame(){
			setTitle("NaverHDScrapper");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(800,300);
			setResizable(false);

			urlLabel = new JLabel("URL 주소");
			postLabel = new JLabel("포스트 주소");
			resultLabel = new JLabel("결과값(720P) : ");
			resultLabel2 = new JLabel("결과값(1080P) : ");
			infoLabel = new JLabel("<HTML>1. 네이버 동영상 [퍼가기] > URL 주소, 포스트 주소 값을 복사하여 넣습니다<br>"
					+"2. [시작] 버튼을 누릅니다.<br>"
					+"3. 결과값의 링크를 들어가시면 HD 동영상이 되어 있습니다. [퍼가기] 하시면 됩니다.<br><br>"
					+"<font color=\"#FF88A8DF\">(자동재생, 시작시간 지정 팁)</font><br></HTML>");
			linkLabel = new JLabel("<HTML><a href=\"#\">"
					+"http://blog.naver.com/bluefox1543/220203793929</a></HTML>");

			linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			linkLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()>0){
						if(Desktop.isDesktopSupported()){
							Desktop desktop = Desktop.getDesktop();
							try {
								URI uri = new URI("http://blog.naver.com/bluefox1543/220203793929");
								desktop.browse(uri);
							} catch (URISyntaxException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			});

			resultField = new JLabel();
			resultField2 = new JLabel();

			setupLine = new JPanel();
			inputLine = new JPanel();
			postLine = new JPanel();

			urlinput = new JTextField(50);
			urlinput.setEditable(true);
			postinput = new JTextField(50);
			postinput.setEditable(true);
			inputbtn = new JButton("시작");
			inputbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							URLConnection conn;
							try {
								String movieString = ProcessPostDocument(postinput.getText().trim());

								if(movieString == null){
									resultField.setText("잘못된 포스트 주소값입니다. 확인 후 다시 입력해 주세요.");
									return;
								}

								String urlString = PreURLFix(urlinput.getText().trim());

								if(urlString == null){
									resultField.setText("잘못된 URL 주소값입니다. 확인 후 다시 입력해 주세요.");
									return;
								}

								URL url = new URL(urlString);

								conn = url.openConnection();
								conn.setUseCaches(false);
								conn.setRequestProperty("User-Agent", userAgent);

								videoURLString = MakeResultURL(movieString,
										ProcessXMLDocument(conn.getInputStream(),1),1);

								if(videoURLString.equals("720P OR 1080P HD 영상이 지원되지 않는 동영상 입니다.")){
									resultField.setText(videoURLString);
									resultField.setCursor(null);
									return;
								}
								
								MouseListener[] mList = resultField.getMouseListeners();

								for(MouseListener m : mList){
									resultField.removeMouseListener(m);
								}

								resultField.setText("<HTML><a href=\"#\">"+videoURLString + "</a></HTML>");
								resultField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
								resultField.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										if(e.getClickCount()>0){
											if(Desktop.isDesktopSupported()){
												Desktop desktop = Desktop.getDesktop();
												try {
													URI uri = new URI(videoURLString + "&width=720&height=438&ispublic=true");
													desktop.browse(uri);
												} catch (URISyntaxException e1) {
													e1.printStackTrace();
												} catch (IOException e1) {
													e1.printStackTrace();
												}
											}
										}
									}
								});
								
								conn = url.openConnection();
								conn.setUseCaches(false);
								conn.setRequestProperty("User-Agent", userAgent);
								
								videoURLString2 = MakeResultURL(movieString,
										ProcessXMLDocument(conn.getInputStream(),2),2);
								
								if(videoURLString2.equals("1080P HD 영상이 지원되지 않는 동영상 입니다.")){
									resultField2.setText(videoURLString2);
									resultField2.setCursor(null);
									return;
								}
								
								mList = resultField2.getMouseListeners();

								for(MouseListener m : mList){
									resultField2.removeMouseListener(m);
								}
								
								resultField2.setText("<HTML><a href=\"#\">"+videoURLString2 + "</a></HTML>");
								resultField2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
								resultField2.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										if(e.getClickCount()>0){
											if(Desktop.isDesktopSupported()){
												Desktop desktop = Desktop.getDesktop();
												try {
													URI uri = new URI(videoURLString2 + "&width=1080&height=720&ispublic=true");
													desktop.browse(uri);
												} catch (URISyntaxException e1) {
													e1.printStackTrace();
												} catch (IOException e1) {
													e1.printStackTrace();
												}
											}
										}
									}
								});

							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						private String ProcessPostDocument(String postURL){
							String resultURL = null;
							URLConnection conn;
							URL url;

							if(!postURL.contains("blog.naver.com"))
								return null;

							try {
								postURL = "http://blog.naver.com/PostView.nhn?blogId=" 
										+ postURL.split("/")[3] + "&logNo=" 
										+ postURL.split("/")[4];

								url = new URL(postURL);
								conn = url.openConnection();
								conn.setUseCaches(false);
								conn.setRequestProperty("User-Agent", userAgent);

								String tmp,doc = "";
								BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

								while((tmp = br.readLine()) != null){
									doc += tmp;
								}

								if(doc.contains("http://blog.naver.com/MultimediaFLVPlayer.nhn")){
									resultURL = doc.substring(doc.indexOf("http://blog.naver.com/MultimediaFLVPlayer.nhn")
											,doc.indexOf("&ispublic") - 21);
								}

							} catch (IOException e) {
								e.printStackTrace();
							}

							return resultURL;
						}

						private String PreURLFix(String urlString){
							String result;
							if(!urlString.contains("vid") || !urlString.contains("outKey")){
								return null;
							}

							String[] params = urlString.split("\\?")[1].split("&");
							result = "http://serviceapi.nmv.naver.com/flash/play.nhn?"
									+params[0] +"&" +params[1];
							return result;
						}

						private String ProcessXMLDocument(InputStream XMLStream,int flag){
							String result = null;

							try {
								DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
								DocumentBuilder builder = df.newDocumentBuilder();

								Document doc = builder.parse(XMLStream);

								NodeList nodelist = doc.getElementsByTagName("EncodingOptions");

								Node node = nodelist.item(0);
								Element element = (Element) node;
								NodeList optionList = element.getElementsByTagName("EncodingOption");
								for(int t = 0; t < optionList.getLength(); t++){
									Element Optionlement = (Element) optionList.item(t);
									NodeList vidList = Optionlement.getElementsByTagName("vid");
									String vid = vidList.item(0).getTextContent();

									NodeList nameList = Optionlement.getElementsByTagName("encodingOptionName");
									String optname = nameList.item(0).getTextContent();
									
									if(optname.equals("720P") && flag == 1){
										result = "vid=" + vid;
									}
									if(optname.equals("1080P") && flag == 2){
										result = "vid=" + vid;
									}
								}

								XMLStream.close();
							} catch (ParserConfigurationException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (SAXException e) {
								e.printStackTrace();
							}

							return result;
						}
						private String MakeResultURL(String processed,String vidString,int flag){
							if(vidString == null){
								if(flag == 1){
									return "720P OR 1080P HD 영상이 지원되지 않는 동영상 입니다.";
								}
								else if(flag == 2){
									return "1080P HD 영상이 지원되지 않는 동영상 입니다.";
								}
							}

							String[] params = processed.split("\\?")[1].split("&");

							return "http://blog.naver.com/MultimediaFLVPlayer.nhn?"
							+ params[0] + "&"
							+ params[1] + "&"
							+ vidString;
						}
					}).start();
				}
			});
			inputLine.add(urlLabel);
			inputLine.add(urlinput);
			inputLine.add(inputbtn);

			postLine.add(postLabel);
			postLine.add(postinput);

			setupLine.setLayout(new GridLayout(2,1));
			setupLine.add(inputLine);
			setupLine.add(postLine);

			infoPanel = new JPanel();
			infoPanel.setLayout(new BorderLayout());
			infoPanel.add(infoLabel,BorderLayout.PAGE_START);
			infoPanel.add(linkLabel,BorderLayout.CENTER);
			infoPanel.add(new JLabel(" "),BorderLayout.PAGE_END);

			resultPanel = new JPanel();
			resultPanel.setLayout(new GridLayout(2,1));
			JPanel tempPanel1 = new JPanel(new BorderLayout());
			JPanel tempPanel2 = new JPanel(new BorderLayout());
			tempPanel1.add(resultLabel, BorderLayout.WEST);
			tempPanel1.add(resultField, BorderLayout.CENTER);
			tempPanel2.add(resultLabel2, BorderLayout.WEST);
			tempPanel2.add(resultField2, BorderLayout.CENTER);
			resultPanel.add(tempPanel1);
			resultPanel.add(tempPanel2);

			setLayout(new BorderLayout());
			add(setupLine,BorderLayout.PAGE_START);
			add(infoPanel,BorderLayout.CENTER);
			add(resultPanel,BorderLayout.PAGE_END);
			setVisible(true);
		}
	}
}