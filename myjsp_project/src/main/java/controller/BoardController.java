package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import domain.BoardVO;
import domain.PagingVO;
import handler.PagingHandler;
import net.coobird.thumbnailator.Thumbnails;
import service.BoardService;
import service.BoardServiceImpl;
import service.CommentService;


@WebServlet("/brd/*")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//로그 객체 선언
	private static final Logger log = LoggerFactory.getLogger(BoardController.class);
	//reqeustDespatcher객체
	private RequestDispatcher rdp;
	//service interface
	private BoardService bsv;
	private CommentService csv;

	private String destPage; //destPage : 목적지 주소 저장변수
	
	private int isOk;
	
	private String savePath; //파일경로를 저장할 변수
	
    public BoardController() {
//        super();
    	//bsv의 객체 연결
    	bsv = new BoardServiceImpl();
    }
    
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     log.info("BoardController의 서비스 함수 시작11111111111111111111111111111");
    //encoding 설정, contentType설정 요청경로파악
    request.setCharacterEncoding("utf-8");
    response.setCharacterEncoding("utf-8");
    response.setContentType("utf-8/html; charset=UTF-8");
    
    //jsp에서 오는 요청주소
    String uri = request.getRequestURI(); // /brd/register
    String path = uri.substring(uri.lastIndexOf("/")+1);
    log.info("path>>>>> "+path);
    log.info("switch_case문 바로 위");
    switch(path) {
    case "register":
    	try {
			log.info("register진입");
			//목적지 주소 설정
			destPage = "/board/register.jsp";
		} catch (Exception e) {
			e.printStackTrace();
		}
    	break; //register끝
    	
    case "insert":
    	try {
			//이미지파일을 업로드할 물리적경로 설정(업로드할때 설정)
    		savePath = getServletContext().getRealPath("/_fileUpload");//상대경로가져오기
    		File fileDir = new File(savePath);
    		log.info("파일저장위치(savePath):"+savePath);
    		//D:\전경환\_myjsp_workspace_paging_com\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\myjsp_project\_fileUpload
    	
    		//파일 객체를 생성하기 위한 객체(파일에 대한 정보를 설정)
    		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
    		fileItemFactory.setRepository(fileDir);//저장할 위치 set(file객체로 지정)(이미지셋은 이렇게...)
    		fileItemFactory.setSizeThreshold(5*1024*1024); //저장을 위한 임시메모리용량 5메가(모자르면 자동으로 늘려줌 바이트단위)
    		BoardVO bvo = new BoardVO();
    		
    		//multipart/form-data형식으로 넘어온 reqeust객체를 다루기 쉽게 변환해주는 객체형식으로 저장
    		ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory); //요기서 fileItemFactory 담아야함
    		
    		List<FileItem> itemList = fileUpload.parseRequest(request);//apach.common꺼 
    		//DB로 넘기기 위한 BoardVO객체로 변환 (경로와 파일이름만...) 이미지는 저장   이미지는 따로 파일 업로드 위치에 업로드 될것임
    	
    		//bvo로 변환 예정
    		//리스트에서 빼올것임
    		for(FileItem item : itemList) {
    			switch(item.getFieldName()) {
    			case "title":
    				bvo.setTitle(item.getString("utf-8"));//인코딩형식을 담아서 변환 한글떄문
    				break;
    			case "writer":
    				bvo.setWriter(item.getString("utf-8"));
    				break;
    			case "content":
    				bvo.setContent(item.getString("utf-8"));
    				break;
    			case "image_file":
    				//이미지 저장 처리 필요
    				//이미지가 필수X 없는 경우에도 처리
    				//이미지가 있는지 체크
    				if(item.getSize()>0) { //데이터 크기가 있다면 이미지 있음처리
    					String fileName = item.getName()	//가끔 경로를 포함해서들어오는 케이스때문에
    							.substring(item.getName().indexOf("/")+1);//파일이름만 분리
    				//시스템 현재시간_파일이름.jpg
    				fileName= System.currentTimeMillis()+"_"+fileName;
    				
    				//파일 객체 생성 : D:~/fileUpload/시간_cat2.jpg
    				//에러생기면 보면 아래 경로에 폴더 하나가 안보일 거임 그냥 윈도우 탐색기에서 만들어 버리기
    				//예전경로--> D:\전경환\_jsp_workspace_paging\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\jsp_project\_fileUpload\1695114911351_Aclass.jpg
    		
    				log.info("fileDir은 " + fileDir );
    				log.info("fileName은 "  + fileName );
    				File uploadFilePath = new File(fileDir+File.separator+fileName);
    				log.info("파일경로+이름 >>" + uploadFilePath);
    				
    				//저장
    				try {
						item.write(uploadFilePath); // 자바 객체를 디스크에 쓰기
						bvo.setImage_File(fileName); //디비에 텍스트 넣기?
						
						//썸네일 작업 : 트래픽 과다사용 방지
						Thumbnails.of(uploadFilePath).size(60,60)
								.toFile(new File(fileDir+File.separator+"_th_"+fileName));
					} catch (Exception e) {
						log.info(">>> file writer on disk 에러입니다");
						e.printStackTrace();
					}
    				}//이미지가 있으면 실행끝
    				break;//이미지파일 끝 (case "insert":안)
    				
    			}//리스트의 for문의 스위치문끝
    		}//for문긑
    		isOk = bsv.register(bvo);
    		log.info(">>>>insert >> " + (isOk>0?"OK":"Fail" ) );
    		destPage="pageList"; //pageList가 없으면 계속 서비스 무한루프돔
    		
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	break;  //case "insert" : 끝
    	
    	
    case "pageList":
    	try {
    	//jsp에서 파라미터 받기
    	PagingVO pgvo = new PagingVO();
    	log.info("페이징전?");
    	if(request.getParameter("pageNo")!=null) {
    		log.info("request.getParameter(\"pageNo\")가 있는 경우 페이징 진입");
    		int pageNo = Integer.parseInt(request.getParameter("pageNo"));//대소문자 조심
    		int qty = Integer.parseInt(request.getParameter("qty"));
    		log.info("pageNo는 "+pageNo+" qty는 "+qty);
    		pgvo = new PagingVO(pageNo, qty); //값이 있으면
    	}
    	
    	//검색어 받기
    	pgvo.setType(request.getParameter("type"));//넘긴 키가  "type", "typeee132" 등으로 이름자체가 없어도 null로 됨 에러는 안남
    	pgvo.setKeyword(request.getParameter("keyword")); //기역우는 .jsp에서 type keyword라는 값으로 넘길 예정
    	log.info("type: "+ pgvo.getType() + "  keyword: "+ pgvo.getKeyword());
    	
    	//PagingVO totalCount
    	int totalCount = bsv.getTotalCount(pgvo); //DB에 전체 카운트 요청
    	log.info("전체게시글 수 " + totalCount );
    	//bsv pgvo주고 ,limit적용한 row10개 가져오기.
    	List<BoardVO> list = bsv.getPageList(pgvo);
    	log.info("pagestart "+pgvo.getPageNo());//DB에서 조회할 시작row인덱스 구하기
    	request.setAttribute("list", list); //다음페이지jsp페이지에서 list라고 쓰면 인식하기 시작
    	log.info("list를 request에 key를 list로 set해줌 "+ list);
    	//페이지 정보를 list.jsp로 보내기
    	log.info("ph = new PagingHandler("+pgvo+","+totalCount+") 직전임");
    	PagingHandler ph = new PagingHandler(pgvo, totalCount);
    	log.info("ph는 페이지 하단 번호를 관리하는 부분이고 값은 " + ph +" 입니다.");
    	request.setAttribute("ph", ph);
    	log.info("paging 끝~!!");
    	destPage="/board/list.jsp";
		} catch (Exception e) {
			log.info("pageList 에러");
			e.printStackTrace();
		}
    	break; //pageList끝
    
    case "count":
    	log.info("case count진입");
    	try {
			int bno = Integer.parseInt(request.getParameter("bno")); //list.jsp에서 bno를 겟으로 넘겨서 여기서 받음 //request셋개념(겟방식?)으로 넘긴듯
			log.info("hitcount직전");
			bsv.hitcount(bno);
			
			destPage="/brd/detail";
		} catch (Exception e) {
			log.info("count 에러");
			e.printStackTrace();
		}
    	break; //case count끝
    	
    case "detail":
    	log.info("case detail진입");
    	try {
    		int bno = Integer.parseInt(request.getParameter("bno")); //위 196줄에서 받은것을 다시 사용 가능한듯 리퀘스트영역에 셋하면 계속 유지되는 듯
    		BoardVO bvo = bsv.detailview(bno); //select * from board 실행목적
			log.info("case detail의 bvo : " + bvo);
			request.setAttribute("bvo", bvo); //request 키 벨류 로 저장함. 바로뒤에 detail.jsp에서 쓰려고...
			destPage = "/board/detail.jsp";
		} catch (Exception e) {
			log.info("detail 에러");
			e.printStackTrace();
		}
    	
    }//switch case문끝
    
    //.jsp파일이 아니라면> 다시 컨트롤러의 case문 탐색
    rdp = request.getRequestDispatcher(destPage);
    rdp.forward(request, response); //대충 위에 꺼랑 한세트

    log.info("서비스함수 끝222222222222222222222222222222222222222222");
    }//service함수 끝
    


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info("보드컨트롤로의 두겟");
		service(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info("보드컨트롤로의 두포스트");
		service(request, response);
	}

}
