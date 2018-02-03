package ru.radiationx.anilibria.entity.app.updater

/**
 * Created by radiationx on 28.01.18.
 */

/*{
    "update":{
    "version_code":"69",
    "version_name":"0.6.7.1",
    "version_build":"11547",
    "build_date":"13 декабря 2017 г.",
    "link_github":"https://raw.githubusercontent.com/RadiationX/ForPDA/master/ForPDA-0.6.7.1.apk",
    "link_4pda":"https://4pda.ru/forum/dl/post/11712694/ForPDA-0.6.7.1.apk",
    "changes":{
    "important":[
    "Раздел новостей в последнее время часто изменяется, поэтому клиент может работать нестабильно. Просьба отнестись с пониманием, в случае чего стараемся быстро выпустить исправление"
    ],
    "added":[
    ],
    "fixed":[
    "Просмотр новостей"
    ],
    "changed":[
    ]
}
},
    "notice":{
    "text":""
}
}*/
class UpdateData {
    var code: Int = 0
    var build: Int = 0
    var name: String? = null
    var date: String? = null
    var links = mutableListOf<UpdateLink>()

    var important = mutableListOf<String>()
    var added = mutableListOf<String>()
    var fixed = mutableListOf<String>()
    var changed = mutableListOf<String>()

    class UpdateLink {
        lateinit var name: String
        lateinit var url: String
        lateinit var type: String
    }
}