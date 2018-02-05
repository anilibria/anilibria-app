String.prototype.format = String.prototype.f = function () {
    var args = arguments;
    return this.replace(/\{(\d+)\}/g, function (m, n) {
        return args[n] ? args[n] : m;
    });
};

const POST_CONTENT_4PDA = "[size=4][b]ForPDA New[/size][/b]\n[size=3][b]версия: {0}[/b][/size]\n\n[b]Что нового: [/b]\n{1}\n[b]Скачать: [/b]{2}"

var jsonField;

var post4pdaField;
var postGpField;
var resultJson;

var versionCodeField;
var versionNameField;
var versionBuildField;
var buildDateField;
var linkGitHubField;
var link4pdaField;
var importantField;
var addedField;
var fixedField;
var changedField;
window.addEventListener("load", function () {
    if (window.File && window.FileReader && window.FileList && window.Blob) {
        document.getElementById('files').addEventListener('change', handleFileSelect, false);
    } else {
        alert('The File APIs are not fully supported in this browser.');
    }

    jsonField = document.getElementById("text_json");

    post4pdaField = document.getElementById("post_4pda");
    postGpField = document.getElementById("post_gp");
    resultJson = document.getElementById("result_json");

    versionCodeField = document.getElementById("version_code");
    versionNameField = document.getElementById("version_name");
    versionBuildField = document.getElementById("version_build");
    buildDateField = document.getElementById("build_date");
    linkGitHubField = document.getElementById("link_site");
    link4pdaField = document.getElementById("link_gp");
    importantField = document.getElementById("important");
    addedField = document.getElementById("added");
    fixedField = document.getElementById("fixed");
    changedField = document.getElementById("changed");
});

function fillFields() {
    var jsonObject = JSON.parse(jsonField.value);
    var updateObject = jsonObject["update"];

    versionCodeField.value = updateObject["version_code"];
    versionNameField.value = updateObject["version_name"];
    versionBuildField.value = updateObject["version_build"];
    buildDateField.value = updateObject["build_date"];
    /*linkGitHubField.value = updateObject["link_github"];
    link4pdaField.value = updateObject["link_4pda"];*/

    var changesObject = updateObject["changes"];
    importantField.value = transformFromArray(updateObject["important"]);
    addedField.value = transformFromArray(updateObject["added"]);
    fixedField.value = transformFromArray(updateObject["fixed"]);
    changedField.value = transformFromArray(updateObject["changed"]);

}

function transformFromArray(jsonArray) {
    var result = "";
    for (var i = 0; i < jsonArray.length; i++) {
        result += jsonArray[i];
        if (i + 1 < jsonArray.length) {
            result += "\n";
        }
    }
    return result;
}

function transformToArray(fieldText) {
    var result = [];
    var lines = fieldText.split("\n");
    for (var i = 0; i < lines.length; i++) {
        if (lines[i].length > 0) {
            result.push(lines[i]);
        }
    }
    return result;
}

function makeChangeBlock(title, changesContent) {
    var result = "";
    result += title + "\n";
    var changesArray = changesContent.split("\n");
    for (var i = 0; i < changesArray.length; i++) {
        result += "– " + changesArray[i];
        /*if (i + 1 < changesArray.length) {
            result += "\n";
        }*/
        result += "\n";
    }
    result += "\n";
    return result;
}

function makePost4pda() {
    var changesContent = "";
    var importantContent = importantField.value;
    var addedContent = addedField.value;
    var fixedContent = fixedField.value;
    var changedContent = changedField.value;
    if (importantContent.length > 0) {
        changesContent += makeChangeBlock("[B]Важное:[/B]", importantContent);
    }
    if (addedContent.length > 0) {
        changesContent += makeChangeBlock("[B]Добавлено:[/B]", addedContent);
    }
    if (fixedContent.length > 0) {
        changesContent += makeChangeBlock("[B]Исправлено:[/B]", fixedContent);
    }
    if (changedContent.length > 0) {
        changesContent += makeChangeBlock("[B]Изменено:[/B]", changedContent);
    }
    var versionName = versionNameField.value;
    var link4pda = link4pdaField.value;

    var result = POST_CONTENT_4PDA.f(versionName, changesContent, link4pda);

    post4pdaField.value = result;
}

function makePostGp() {
    var changesContent = "";
    var importantContent = importantField.value;
    var addedContent = addedField.value;
    var fixedContent = fixedField.value;
    var changedContent = changedField.value;
    if (importantContent.length > 0) {
        changesContent += makeChangeBlock("Важное:", importantContent);
    }
    if (addedContent.length > 0) {
        changesContent += makeChangeBlock("Добавлено:", addedContent);
    }
    if (fixedContent.length > 0) {
        changesContent += makeChangeBlock("Исправлено:", fixedContent);
    }
    if (changedContent.length > 0) {
        changesContent += makeChangeBlock("Изменено:", changedContent);
    }
    var versionName = versionNameField.value;
    var link4pda = link4pdaField.value;

    var result = changesContent;

    postGpField.value = result;
    if (result.length > 500) {
        if (!postGpField.classList.contains("error")) {
            postGpField.classList.add("error");
        }
    } else {
        postGpField.classList.remove("error");
    }
}

function makeJson() {
    var jsonObject = {};
    jsonObject.update = {};

    var updateObject = jsonObject.update;
    updateObject.version_code = versionCodeField.value;
    updateObject.version_name = versionNameField.value;
    updateObject.version_build = versionBuildField.value;
    updateObject.build_date = buildDateField.value;
    updateObject.links = [];
    updateObject.links.push({
        name: "AniLibria",
        url: linkGitHubField.value,
        type: "file"
    });
    updateObject.links.push({
        name: "Play Market",
        url: link4pdaField.value,
        type: "site"
    });

    updateObject.important = transformToArray(importantField.value);
    updateObject.added = transformToArray(addedField.value);
    updateObject.fixed = transformToArray(fixedField.value);
    updateObject.changed = transformToArray(changedField.value);

    var result = JSON.stringify(jsonObject, "", 4);

    resultJson.value = result;
}

function makeContent() {
    makePost4pda();
    makePostGp();
    makeJson();
}

function handleFileSelect(evt) {
    var files = evt.target.files; // FileList object
    var file = files[0];

    var reader = new FileReader();
    reader.onload = (function (theFile) {
        return function (e) {
            jsonField.value = e.target.result;
        };
    })(file);
    reader.readAsText(file);
}
