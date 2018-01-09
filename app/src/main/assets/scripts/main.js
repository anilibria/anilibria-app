var Base64 = {
    characters: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

    encode: function (string) {
        var characters = Base64.characters;
        var result = '';

        var i = 0;
        do {
            var a = string.charCodeAt(i++);
            var b = string.charCodeAt(i++);
            var c = string.charCodeAt(i++);

            a = a ? a : 0;
            b = b ? b : 0;
            c = c ? c : 0;

            var b1 = (a >> 2) & 0x3F;
            var b2 = ((a & 0x3) << 4) | ((b >> 4) & 0xF);
            var b3 = ((b & 0xF) << 2) | ((c >> 6) & 0x3);
            var b4 = c & 0x3F;

            if (!b) {
                b3 = b4 = 64;
            } else if (!c) {
                b4 = 64;
            }

            result += Base64.characters.charAt(b1) + Base64.characters.charAt(b2) + Base64.characters.charAt(b3) + Base64.characters.charAt(b4);

        } while (i < string.length);

        return result;
    },

    decode: function (string) {
        var characters = Base64.characters;
        var result = '';

        var i = 0;
        do {
            var b1 = Base64.characters.indexOf(string.charAt(i++));
            var b2 = Base64.characters.indexOf(string.charAt(i++));
            var b3 = Base64.characters.indexOf(string.charAt(i++));
            var b4 = Base64.characters.indexOf(string.charAt(i++));

            var a = ((b1 & 0x3F) << 2) | ((b2 >> 4) & 0x3);
            var b = ((b2 & 0xF) << 4) | ((b3 >> 2) & 0xF);
            var c = ((b3 & 0x3) << 6) | (b4 & 0x3F);

            result += String.fromCharCode(a) + (b ? String.fromCharCode(b) : '') + (c ? String.fromCharCode(c) : '');

        } while (i < string.length);

        return result;
    }
};


function NativeEvents() {
    this.DOM = "DOMContentLoaded";
    this.PAGE = "load";

    const DOM = this.DOM;
    const PAGE = this.PAGE;
    const LOG_TAG = "JS event: ";

    var nativeDomComplete = [];
    var nativePageComplete = [];
    var instantDomComplete = [];
    var instantPageComplete = [];

    function onNativeDomComplete() {
        console.log(LOG_TAG + "onNativeDomComplete");
        functionCaller(nativeDomComplete);
    }

    function onNativePageComplete() {
        console.log(LOG_TAG + "onNativePageComplete");
        functionCaller(nativePageComplete);
    }

    function functionCaller(funcArray) {
        while (funcArray.length > 0) {
            var func = funcArray.shift();
            try {
                console.log(LOG_TAG + "Call function: '" + func.name + "'");
                func();
            } catch (e) {
                console.error(e);
            }
        }
    }

    document.addEventListener(DOM, function (e) {
        console.log(LOG_TAG + DOM);
        if (instantDomComplete.length > 0) {
            console.log(LOG_TAG + "Call instant functions");
            functionCaller(instantDomComplete);
        }
        if (typeof IBase != 'undefined') {
            IBase.domContentLoaded();
        } else {
            onNativeDomComplete();
        }
    });
    window.addEventListener(PAGE, function (e) {
        console.log(LOG_TAG + PAGE);
        if (instantPageComplete.length > 0) {
            console.log(LOG_TAG + "Call instant functions");
            functionCaller(instantPageComplete);
        }
        if (typeof IBase != 'undefined') {
            IBase.onPageLoaded();
        } else {
            onNativePageComplete();
        }
    });

    this.addEventListener = function (name, func, instantly) {
        instantly = Boolean(instantly | false);
        try {
            if (name == undefined | name == null | typeof name != "string") {
                throw new Error("Name invalid");
            }
            if (func == undefined | func == null | typeof func != "function") {
                throw new Error("Function invalid")
            }
            if (name === DOM) {
                if (instantly) {
                    instantDomComplete.push(func);
                } else {
                    nativeDomComplete.push(func);
                }
            }
            if (name === PAGE) {
                if (instantly) {
                    instantPageComplete.push(func);
                } else {
                    nativePageComplete.push(func);
                }
            }
        } catch (err) {
            console.error(err);
        }
    }

    this.onNativeDomComplete = function () {
        onNativeDomComplete();
    }

    this.onNativePageComplete = function () {
        onNativePageComplete();
    }
}
var nativeEvents = new NativeEvents();

function Base64Encode(str) {
    var encoding = 'utf-8';
    var bytes = new TextDecoderLite(encoding).encode(str);
    return base64js.fromByteArray(bytes);
}

function Base64Decode(str) {
    var encoding = 'utf-8';
    var bytes = base64js.toByteArray(str);
    return new TextDecoderLite(encoding).decode(bytes)
}

function nodeScriptReplace(node) {
    if (nodeScriptIs(node) === true) {
        node.parentNode.replaceChild(nodeScriptClone(node), node);
    } else {
        var i = 0;
        var children = node.childNodes;
        while (i < children.length) {
            nodeScriptReplace(children[i++]);
        }
    }

    return node;
}

function nodeScriptIs(node) {
    return node.tagName === 'SCRIPT';
}

function nodeScriptClone(node) {
    var script = document.createElement("script");
    script.text = node.innerHTML;
    for (var i = node.attributes.length - 1; i >= 0; i--) {
        script.setAttribute(node.attributes[i].name, node.attributes[i].value);
    }
    return script;
}

function ViewModelClass() {
    this.setText = function (id, text) {
        text = Base64Decode(text);
        document.getElementById(id).innerHTML = text;
        try {
            nodeScriptReplace(document.getElementById(id));
        } catch (ex) {
            console.error(ex);
        }
        try {
            onAnyLoad();
        } catch (ex) {
            console.error(ex);
        }
    }
}

var ViewModel = new ViewModelClass();




/*(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s);
    js.id = id;
    js.src = "//vk.com/js/api/openapi.js?151";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'vk_openapi_js'));
(function () {
    if (!window.VK || !VK.Widgets || !VK.Widgets.Post || !VK.Widgets.Post('vk_post_1_45616', 1, 45616, 'Zs8yZtljpQpNKslXB8Cr361kKAlt')) setTimeout(arguments.callee, 50);
}());*/
