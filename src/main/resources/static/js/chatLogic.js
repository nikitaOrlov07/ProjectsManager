let stompClient = null;
let chatId, username,participants;
let isFirstJoin = true; // Flag to track the first join

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
        updateUserStatus(username, true);
        stompClient.send("/app/chat/" + chatId + "/userStatus", {}, JSON.stringify({username: username, status: 'ONLINE'}));
    });
}

function subscribeToChat(chatId) {
    stompClient.subscribe('/topic/chat/' + chatId, function (response) {
        console.log('Получено сообщение:', response.body);
        const message = JSON.parse(response.body);
        if (message.type === 'JOIN' && isFirstJoin) {
            showJoinMessage(message);
            isFirstJoin = false; // Сбрасываем флаг после первого присоединения
        } else if (message.type !== 'JOIN') {
            showMessage(message);
        }
    });
}

function showJoinMessage(message) {
    const chatContainer = document.querySelector('.projects-list');
    const joinMessageDiv = document.createElement('div');
    joinMessageDiv.className = 'join-message';
    joinMessageDiv.textContent = `${message.sender} присоединился к чату`;
    chatContainer.appendChild(joinMessageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}
function showMessage(message) {
    // Проверка на наличие текста сообщения
    if (!message.text || message.text.trim() === '') {
        console.log('Получено пустое сообщение:', message);
        return; // Не отображаем пустые сообщения
    }

    const chatContainer = document.querySelector('.projects-list');
    const currentUsername = document.getElementById('chat-page').dataset.username;

    const messageDiv = document.createElement('div');
    const messageContent = document.createElement('div');
    const authorParagraph = document.createElement('p');
    const textParagraph = document.createElement('p');
    const dateParagraph = document.createElement('p');

    authorParagraph.className = 'message-author';
    textParagraph.className = 'message-text';
    dateParagraph.className = 'message-text';

    if (message.author === currentUsername) {
        messageDiv.className = 'message message-right';
        authorParagraph.textContent = 'You:';

        const deleteForm = document.createElement('form');
        deleteForm.method = 'post';
        deleteForm.action = `/comments/${message.chatId}/delete/${message.id}`;
        deleteForm.style = 'position: absolute;right: 5px; top: 0;';

        const deleteButton = document.createElement('button');
        deleteButton.className = 'btn btn-primary';
        deleteButton.textContent = 'Delete';

        deleteForm.appendChild(deleteButton);
        messageContent.appendChild(deleteForm);
    } else {
        messageDiv.className = 'message message-left';
        authorParagraph.textContent = message.author;
    }

    textParagraph.textContent = message.text;
    dateParagraph.textContent = message.pubDate || new Date().toLocaleString();

    messageContent.style = 'display: flex; justify-content: space-between; flex-grow: 1;';
    messageContent.appendChild(authorParagraph);
    messageContent.appendChild(textParagraph);

    messageDiv.appendChild(messageContent);
    messageDiv.appendChild(dateParagraph);

    chatContainer.appendChild(messageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
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
                        console.log("User added to chat");
                        window.history.pushState({}, '', '/chat/' + chatId);
                        isFirstJoin = true; // Сбрасываем флаг, так как это новый чат
                    }
                } else {
                    console.log('User already in chat');
                    isFirstJoin = false;
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
        const message = {
            author: username,
            text: messageContent,
            type: 'CHAT'
        };
        stompClient.send("/app/chat/" + chatId + "/sendMessage", {}, JSON.stringify(message));
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