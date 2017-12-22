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


function ViewModelClass() {
    this.setText = function (id, text) {
        document.getElementById(id).innerHTML = text;
        onAnyLoad();
    }
}

var ViewModel = new ViewModelClass();
