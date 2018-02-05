var config = {
    fields: [
        {
            id: "version_code",
            type: "string",
            title: "Код версии"
        },
        {
            id: "version_build",
            type: "string",
            title: "Версия сборки"
        },
        {
            id: "version_name",
            type: "string",
            title: "Имя версии"
        },
        {
            id: "build_date",
            type: "string",
            title: "Дата сборки"
        },
        {
            id: "links",
            type: "array",
            title: "Ссылки",
            fields: [
                {
                    id: "name",
                    type: "string"
                },
                {
                    id: "url",
                    type: "string"
                },
                {
                    id: "type",
                    type: "select",
                    fields: [
                        {
                            id: "file",
                            title: "Файл"
                        },
                        {
                            id: "site",
                            title: "Сайт"
                        }
                    ]
                }
            ],
            default: [
                {
                    name: "hui",
                    type: "file"
                },
                {
                    name: "hui",
                    type: "site"
                }
            ]
        },
        {
            id: "important",
            type: "string",
            title: "Важное",
            multiline: true
        },
        {
            id: "added",
            type: "string",
            title: "Добавлено",
            multiline: true
        },
        {
            id: "fixed",
            type: "string",
            title: "Исправлено",
            multiline: true
        },
        {
            id: "changed",
            type: "string",
            title: "Изменено",
            multiline: true
        }
    ]
}
