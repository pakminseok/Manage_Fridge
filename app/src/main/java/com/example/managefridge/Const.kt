package com.example.managefridge

import android.os.FileObserver.CREATE

const val DB_NAME = "Fridge"
const val DB_VERSION = 1

const val TABLE_FRIDGE = "Fridge"
const val COL_ID = "id"
/*냉장고에 들어가는 재료/음식명*/
const val COL_NAME = "itemName"
/*냉장고 보관날짜(시작날짜)*/
const val COL_CREATED_AT = "createdAt"
/*유통기한*/
const val COL_EXPIRATION_AT = "expirationAt"
/*유통기한 며칠 전에 경고할지 설정*/