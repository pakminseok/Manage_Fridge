# Manage_Fridge
식품의 유통기한을 메모하여, 못 먹고 버리는 재료/음식이 없도록 도와주는 앱. 주말마다 업데이트 예정.
ToDo앱 기반.

![initial](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/requirement.JPG)
  
  
  
# 업데이트 일자(19.12.29)  
  
1. 시작 화면  
-아이콘이 나왔다가 사라지면서 앱이 시작함
![FirstScreen](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/FirstScreen.jpg)

2. 냉장고에 넣은 식품을 보여줌  
-식품명 / 유통기한 / 유통기한까지 남은 일자를 볼 수 있다.  
-유통기한까지 남은 일자가 3일 이내인 경우 -> 배경색 = 빨강색  
-유통기한이 지난 식품-> 배경색 = 회색  
-나머지 식품-> 배경색 = 흰색  
-유통기한 임박한 순서대로 위->아래로 정렬해서 표시됨(유통기한 지난 것이 우선순위가  높음)

![ShowList1](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/ShowList1.png)

3. 식품 추가  
-화면 우측 하단의 + 버튼을 누르면 식품 추가를 할 수 있다.  
-"날짜를 선택하세요"를 누르면 날짜를 선택할 수 있는 캘린더 뷰가 나타난다. 원하는 날짜를 선택하면 된다  
-추가할 식품명과 유통기한을 입력하였으면, "추가" 버튼을 누른다.  
-현재 작업을 취소하고 싶으면 "취소"버튼을 누른다.  
-식품명, 유통기한 중 하나라도 입력을 안 하면, 추가되지 않는다.  
![Add](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/AddItem.png)
![datepicker](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/ChooseExpirationDate.jpg)

4. 편집 메뉴  
-각 식품 목록의 우측에 메뉴버튼이 있다.  
-메뉴 버튼을 클릭하면 수정/삭제 기능을 선택할 수 있다.  
![Menu](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/Menu_Edit_Delete.png)

5. 수정하기  
-식품명이나 유통기한을 수정할 수 있다.  
![edit](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/EditItem.jpg)

6. 삭제하기  
-메뉴 - 삭제를 누르면, 정말로 삭제할 지 한 번더 메시지를 보여준다.  
-"네 삭제할래요"를 누르면 삭제, "아니오"를 누르면 작업 취소다.  
![delete](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/DeleteItem.jpg)

7. 캘린더 모드  
-하단 네비 버튼 중 "일자별 보기"를 선택하면, 달력이 나온다.  
-달력에 원하는 일자를 선택한다.  
-해당 일자에 유통기한인 식품들의 리스트를 볼 수 있다.(특정 일자의 식품만 보기)  
![calendarmode](https://github.com/pakminseok/Manage_Fridge/blob/master/howToImage/CalendarView.png)
