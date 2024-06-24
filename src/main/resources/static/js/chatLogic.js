let stompClient = null;
let chatId, username,participants;

document.addEventListener('DOMContentLoaded', function () {
    const chatContainer = document.getElementById('chat-page');
    if (chatContainer) {
        chatId = chatContainer.dataset.chatId;
        username = chatContainer.dataset.username;
        participants =  chatContainer.dataset.participants;
        connect();
    } else {
        console.error('Element with id "chat-page" not found');
    }

    document.getElementById('messageForm').addEventListener('submit', function(e) {
        e.preventDefault();
        sendMessage();
    });

    const deleteChatForm = document.querySelector('form[action$="/delete"]');
    if (deleteChatForm) {
        deleteChatForm.addEventListener('submit', function(e) {
            e.preventDefault();
            if (confirmDelete()) {
                deleteChat();
            }
        });
    }

    const clearChatForm = document.querySelector('form[action$="/clear"]');
    if (clearChatForm) {
        clearChatForm.addEventListener('submit', function(e) {
            e.preventDefault();
            if (confirmDelete()) {
                clearChat();
            }
        });
    }

    // Инициализация счетчика символов
    updateCharCount();
});

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        subscribeToChat(chatId);
        addUser();
    });
}

function subscribeToChat(chatId) {
    stompClient.subscribe('/topic/chat/' + chatId, function (response) {
        showMessage(JSON.parse(response.body));
    });
}

function addUser() {
    stompClient.send("/app/chat/" + chatId + "/addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN', chatId: chatId}),
        function(response) {
            if (response.body) {
                let message = JSON.parse(response.body);
                if (message) {
                    console.log('User added to chat or chat created');
                    if (message.chatId && message.chatId !== chatId) {
                        // Новый чат был создан, обновляем chatId и подписку
                        chatId = message.chatId;
                        stompClient.unsubscribe('/topic/chat/' + chatId);
                        subscribeToChat(chatId);
                        // Здесь можно добавить логику для обновления URL или других элементов UI
                        window.history.pushState({}, '', '/chat/' + chatId);
                    }
                    // Здесь можно добавить логику для обновления UI
                } else {
                    console.log('User already in chat');
                }
            }
        }
    );
}

function sendMessage() {
    const messageInput = document.getElementById('commentText');
    const messageContent = messageInput.value.trim();
    console.log("Sent message: " + messageContent);
    if (messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT'
        };
        stompClient.send("/app/chat/" + chatId + "/sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    updateCharCount();
}

function deleteMessage(messageId) {
    if (stompClient) {
        stompClient.send("/app/chat/" + chatId + "/deleteMessage", {}, messageId);
    }
}

function clearChat() {
    if (stompClient) {
        stompClient.send("/app/chat/" + chatId + "/clear", {}, {});
    }
}

function deleteChat() {
    if (stompClient) {
        stompClient.send("/app/chat/" + chatId + "/delete", {}, {});
    }
}

function showMessage(message) {
    const chatContainer = document.querySelector('.projects-list');
    const currentUsername = document.getElementById('chat-page').dataset.username;

    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message');

    if (message.author === currentUsername) {
        messageDiv.classList.add('message-right');
        messageDiv.innerHTML = `
            <div style="position: relative">
                <div style="display: flex; justify-content: space-between; flex-grow: 1;">
                    <div>
                        <p class="message-author">You:</p>
                        <p class="message-text">${escapeHtml(message.text)}</p>
                    </div>
                    <div style="position: absolute; right: 0px; bottom: 0px;">
                        <p class="message-text">${message.pubDate}</p>
                    </div>
                    <form style="position: absolute; right: 5px; top: 0;" method="post" action="/messages/${message.chat.id}/delete/${message.id}">
                        <button class="btn btn-primary">Delete</button>
                    </form>
                </div>
            </div>
        `;
    } else {
        messageDiv.classList.add('message-left');
        messageDiv.innerHTML = `
            <div style="display: flex; justify-content: space-between; flex-grow: 1;">
                <div>
                    <p class="message-author">${escapeHtml(message.author)}</p>
                    <p class="message-text">${escapeHtml(message.text)}</p>
                </div>
            </div>
            <div style="align-self: flex-end; text-align: right;">
                <p class="message-text">${message.pubDate}</p>
            </div>
        `;
    }

    chatContainer.appendChild(messageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function updateCharCount() {
    const textarea = document.getElementById('commentText');
    const charCount = textarea.value.length;
    document.getElementById('charCount').textContent = charCount;
}

function confirmDelete() {
    return confirm("Are you sure you want to delete this?");
}