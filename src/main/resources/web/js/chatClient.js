const DEFAULT_TIMEOUT = 500;
const MAX_TIMEOUT = 5000;
const TIMEOUT_INCREASE = 100;

let chatTimer;
let version = 0;
let timeout = DEFAULT_TIMEOUT;

function sendChat() {
    let sendBox = document.getElementById("sendText");
    if (sendBox) {
        let http = new XMLHttpRequest();
        http.open("POST", "send", true);
        http.send("message=" + sendBox.value);

        sendBox.value = "";

        clearTimeout(chatTimer);
        timeout = DEFAULT_TIMEOUT;
        setTimeout(refreshChat, timeout);
    }
}

function updateChat(response) {
    let chatbox = document.getElementById("chatbox");
    if (chatbox) {
        chatbox.value = response;
    }
}

function refreshChat() {
    let http = new XMLHttpRequest();
    http.onreadystatechange = function () {
        if (http.readyState === 4) {
            if (http.status === 200) {
                timeout = DEFAULT_TIMEOUT;
                updateChat(http.responseText);
                refreshVersion();
            } else if (timeout < MAX_TIMEOUT) {
                timeout += TIMEOUT_INCREASE;
            }
            //set timeout for next chat query
            chatTimer = window.setTimeout(refreshChat, timeout);
        }
    };
    http.open("GET", "updatechat" + "?version=" + version, true); // true for asynchronous
    http.send();
}

function refreshVersion() {
    let http = new XMLHttpRequest();
    http.onreadystatechange = function () {
        if (http.readyState === 4 && http.status === 200) {
            version = http.responseText;
        }
    };
    http.open("GET", "chatversion", true);
    http.send();
}

function onPost() {
    setTimeout(function () {
        let messageBox = document.getElementById("message");
        if (messageBox) {
            messageBox.value = "";
        }
    }, 100);
    clearTimeout(chatTimer);
    timeout = DEFAULT_TIMEOUT;
    setTimeout(refreshChat, 100);
    return true;
}

function onSendKey(key) {
    if (key === 13) {
        sendChat()
    }
}

//start everything going
refreshChat();