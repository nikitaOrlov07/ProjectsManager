let stompClient = null;
let chatId, username, participants,messageId;
let isFirstJoin = true; // Flag to track the first join

document.addEventListener('DOMContentLoaded', function () {
    const chatContainer = document.getElementById('chat-page');
    if (chatContainer) {
        chatId = chatContainer.dataset.chatId;
        username = chatContainer.dataset.username;
        participants = chatContainer.dataset.participants;
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

    // Initialize character counter
    updateCharCount();
});
function deleteChat() {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/chat/" + chatId + "/delete", {}, JSON.stringify({chatId: chatId}));
    } else {
        console.error("Cannot delete chat: stompClient is not connected");
    }
}

function handleChatDeleted() {
    const chatContainer = document.querySelector('.projects-list');
    const deletedMessageDiv = document.createElement('div');
    deletedMessageDiv.className = 'chat-deleted-message';
    deletedMessageDiv.textContent = 'This chat has been deleted.';
    chatContainer.innerHTML = ''; // Очищаем чат
    chatContainer.appendChild(deletedMessageDiv);

    // Отключаем форму отправки сообщений
    const messageForm = document.getElementById('messageForm');
    if (messageForm) {
        messageForm.style.display = 'none';
    }

    // Отключаем кнопку удаления чата
    const deleteChatForm = document.querySelector('form[action$="/delete"]');
    if (deleteChatForm) {
        deleteChatForm.style.display = 'none';
    }
}

function connect() {
    console.log("Connecting to WebSocket...");
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        console.log('StompClient ready:', stompClient);
        subscribeToChat(chatId);
        addUser();
    }, function(error) {
        console.error('STOMP error:', error);
    });
}

function subscribeToChat(chatId) {
    stompClient.subscribe('/topic/chat/' + chatId, function (response) {
        console.log('Received message:', response.body);
        const message = JSON.parse(response.body);
        if (message.type === 'JOIN' && isFirstJoin) {
            showJoinMessage(message);
            isFirstJoin = false;
        } else if (message.type === 'DELETE') {
            handleDeletedMessage(message);
        } else if (message.type === 'CHAT_DELETED') {
            handleChatDeleted();
        } else if (message.type !== 'JOIN') {
            showMessage(message);
        }
    });
}

function handleDeletedMessage(message) {
    console.log("Handling deleted message:", message);
    const messageId = message.text; // Assuming the server sends the deleted message ID in the 'text' field
    const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
    if (messageElement) {
        messageElement.remove();
    } else {
        console.error("Could not find message element to delete");
    }
}

function showJoinMessage(message) {
    const chatContainer = document.querySelector('.projects-list');
    const joinMessageDiv = document.createElement('div');
    joinMessageDiv.className = 'join-message';
    joinMessageDiv.textContent = `${message.author} joined the chat`;
    chatContainer.appendChild(joinMessageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

function showMessage(message) {
    // Check for empty message
    if (!message.text || message.text.trim() === '') {
        console.log('Received empty message:', message);
        return; // Don't display empty messages
    }

    const chatContainer = document.querySelector('.projects-list');
    const currentUsername = document.getElementById('chat-page').dataset.username;

    const messageDiv = document.createElement('div');
    messageDiv.dataset.messageId = message.id;
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

        const deleteButton = document.createElement('button');
        deleteButton.className = 'btn btn-primary';
        deleteButton.textContent = 'Delete';
        deleteButton.style.width = 'auto';
        deleteButton.style.height = 'auto';
        deleteButton.style.position = 'absolute';
        deleteButton.style.right = '20px';
        deleteButton.style.top = '3px';
        deleteButton.type = 'button';
        deleteButton.onclick = function(event) {
            event.preventDefault();
            event.stopPropagation();
            console.log("Delete button clicked for message:", message.id);
            deleteMessage(message.id);
        };

        messageDiv.appendChild(deleteButton);
    } else {
        messageDiv.className = 'message message-left';
        authorParagraph.textContent = message.author;
    }

    textParagraph.textContent = message.text;
    dateParagraph.textContent = message.pubDate || new Date().toLocaleString();

    messageContent.style = 'display: flex; justify-content: space-between; flex-grow: 1;';
    messageContent.appendChild(authorParagraph);
    messageContent.appendChild(textParagraph);

    messageDiv.style.position = 'relative';  // Ensure that messageDiv is the positioning context for the delete button
    messageDiv.appendChild(messageContent);
    messageDiv.appendChild(dateParagraph);

    chatContainer.appendChild(messageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}


function addUser() {
    stompClient.send("/app/chat/" + chatId + "/addUser",
        {},
        JSON.stringify({author: username, type: 'JOIN', chat: {id: chatId}}),
        function(response) {
            if (response.body) {
                let message = JSON.parse(response.body);
                if (message) {
                    console.log('User added to chat or chat created');
                    if (message.chat && message.chat.id !== chatId) {
                        // New chat was created, update chatId and subscription
                        chatId = message.chat.id;
                        stompClient.unsubscribe('/topic/chat/' + chatId);
                        subscribeToChat(chatId);
                        console.log("User added to chat");
                        window.history.pushState({}, '', '/chat/' + chatId);
                        isFirstJoin = true; // Reset flag as this is a new chat
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
            type: 'CHAT',
        };
        stompClient.send("/app/chat/" + chatId + "/sendMessage", {}, JSON.stringify(message));
        messageInput.value = '';
    }
    updateCharCount();
}

function deleteMessage(messageId) {
    console.log("deleteMessage function called with messageId:", messageId);
    console.log("Current chatId:", chatId);
    if (stompClient && messageId && chatId) {
        console.log("Attempting to delete message. ChatId:", chatId, "MessageId:", messageId);
        stompClient.send("/app/chat/" + chatId + "/deleteMessage", {}, JSON.stringify({messageId: messageId}));
    } else {
        console.error("Cannot delete message: stompClient is not connected, messageId is undefined, or chatId is undefined");
        console.log("stompClient:", stompClient);
        console.log("messageId:", messageId);
        console.log("chatId:", chatId);
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