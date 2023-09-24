//댓글 등록시 bno, writer, content
document.getElementById('cmtAddBtn').addEventListener('click',()=>{ //detail.jsp에서 id cmtAddBtn로 줌,  클릭하면 동작
	const cmtText = document.getElementById('cmtText').value; //detail.jsp 에서 인풋 버튼 디브 해줌 여기선 불러다 씀
	console.log(cmtText);
	if(cmtText == null || cmtText ==""){
		alert('댓글을 입력해주세요.');
		return false;
	}else{
		let cmtData ={ //키벨류 형식의 오브젝트
			bno : bnoVal,   //detail.jsp에서 const로 bnoVal을 저장해두었음 여기선 불러다 씀
			writer : document.getElementById('cmtWriter').value, //detail.jsp 에서 인풋 버튼 디브 해줬음 여기선 불러다 씀
			content : cmtText  //detail.jsp 에서 인풋 버튼 디브 해줬음 여기선 불러다 씀 이미 우에서 1번 const변수처리 한듯
		};
		//전송 function 호출
		postCommentToServer(cmtData).then(result=>{ //이게 댓글을 디비에 등록하는 부분인듯
			//값을 result로 받음
			if(result>0){
				alert("댓글 등록 성공~!!");
			}else{
				alert("댓글 등록 실패~!!");
			}
			printCommentList(cmtData.bno);  //결국 마지막에는 페이지에서 댓글을 보여주기 해주려고 하는듯... 
		})
	}
})


async function postCommentToServer(cmtData){ //    //비동기 에이싱크 시작
	try{
		const url="/cmt/post"; //전송을 위한 정보 config 필요   //컨트롤러에서 잡기위해 cmt 서블릿 만들기 시작230924_20:05    CommentController.java
		const config = {
			method:'post',
			headers:{
				'Content-Type':'application/json; charset=utf-8'
			},
			body: JSON.stringify(cmtData) //서버가 받을 수 있게 스르링형식으로 만듬 
		};
		const resp = await fetch(url,config); //다 받을 때까지 기다렸다 받아줘~  ==await
		const result = await resp.text(); //0 또는 1(isOk) 결과는 바로 옴   ,  비동기는 보낸곳으로 다시 옴
		
		return result;
	}catch(error){
		console.log(error);
	}
}//async function postCommentToServer(cmtData){    //비동기 에이싱크 끝

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function printCommentList(bno){
    getCommentListFromServer(bno).then(result=>{  //요청하자마자 결과도착하면...?
        console.log(result);
        if(result.length>0){
            spreadCommentList(result);
        }else{
            let div = document.getElementById('accordionExample');
            div.innerHTML = `comment가 없습니다!댓글이 없습니다!`;
        }
    })
}

//서버에 댓글 리스트를 달라고 요청    //서버에 댓글 리스트를 달라고 요청 
async function getCommentListFromServer(bno){
    try {
        const resp = await fetch('/cmt/list/'+bno);  //    /cmt/list/151
        const result = await resp.json(); //결과를 제이슨형태로 받게됨
        return result;        
    } catch (error) {
        console.log(error);
    }
}

function spreadCommentList(result){ //result 댓글 list     //일단 디비에서 result가 있어야함
    console.log(result);
    let div = document.getElementById('accordionExample');
    div.innerHTML="";
    // {[],[],[]}
    for(let i=0; i<result.length; i++){
        let str = `<div class="accordion-item">`;
        str += `<h2 class="accordion-header" id="heading${i}">`; /* i번지 해딩이라는 id 추가 */
        str += `<button class="accordion-button" type="button"`;
        str += `data-bs-toggle="collapse" data-bs-target="#collapse${i}"`;
        str += `aria-expanded="true" aria-controls="collapse${i}"`;
        str += `${result[i].cno}, ${result[i].writer}, ${result[i].reg_date}`;
        str += `</button> </h2>`;
        str += `<div id="collapse${i}" class="accordion-collapse collapse show"`;
        str += `data-bs-parent="#accordionExample">`;
        str += `<div class="accordion-body">`;
        str += `<input type="text" class="form-control" id="cmtText" value="${result[i].content}">`;/* 컨텐츠는 단순히 보여주는것이 아니라 입력도 받을수 있게 한다. */       /* https://getbootstrap.kr/docs/5.3/forms/form-control/  폼컨트롤 가져옴*/ 
        str += `<button type="button" data-cno="${result[i].cno}" data-writer="${result[i].writer}"  class="btn btn-warning cmtModBtn">%</button>`; /* https://getbootstrap.kr/docs/5.3/components/buttons/ */
        str += `<button type="button"  data-cno="${result[i].cno}"  class="btn btn-danger cmtDelBtn">X</button>`;
        str += `</div> </div> </div>`;
        div.innerHTML+= str; // 누적해서 담기
    }
}

























