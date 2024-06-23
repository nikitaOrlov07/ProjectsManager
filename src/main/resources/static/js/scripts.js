/*!
* Start Bootstrap - Grayscale v7.0.6 (https://startbootstrap.com/theme/grayscale)
* Copyright 2013-2023 Start Bootstrap
* Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-grayscale/blob/master/LICENSE)
*/
//
// Scripts
// 

window.addEventListener('DOMContentLoaded', event => {

    // Navbar shrink function
    var navbarShrink = function () {
        const navbarCollapsible = document.body.querySelector('#mainNav');
        if (!navbarCollapsible) {
            return;
        }
        if (window.scrollY === 0) {
            navbarCollapsible.classList.remove('navbar-shrink')
        } else {
            navbarCollapsible.classList.add('navbar-shrink')
        }

    };

    // Shrink the navbar
    navbarShrink();

    // Shrink the navbar when page is scrolled
    document.addEventListener('scroll', navbarShrink);

    // Activate Bootstrap scrollspy on the main nav element
    const mainNav = document.body.querySelector('#mainNav');
    if (mainNav) {
        new bootstrap.ScrollSpy(document.body, {
            target: '#mainNav',
            rootMargin: '0px 0px -40%',
        });
    };

    // Collapse responsive navbar when toggler is visible
    const navbarToggler = document.body.querySelector('.navbar-toggler');
    const responsiveNavItems = [].slice.call(
        document.querySelectorAll('#navbarResponsive .nav-link')
    );
    responsiveNavItems.map(function (responsiveNavItem) {
        responsiveNavItem.addEventListener('click', () => {
            if (window.getComputedStyle(navbarToggler).display !== 'none') {
                navbarToggler.click();
            }
        });
    });



});
function hideAlert(alertId) {
    setTimeout(function() {
        let alertElement = document.getElementById(alertId);
        if (alertElement) {
            alertElement.style.display = 'none'; <!--display property is set to none -> the element will be hidden from the page -->
        }
    }, 5000); // after 5 seconds -> hide alert
}
function updateProgress() {
    const tasks = document.querySelectorAll('.task-list input[type="checkbox"]');
    let completedTasks = 0;
    tasks.forEach(task => {
        if (task.checked) {
            completedTasks++;
        }
    });
    const progress = (completedTasks / tasks.length) * 100;
    const progressBar = document.getElementById('progress-bar');
    progressBar.style.width = progress + '%';
    progressBar.setAttribute('aria-valuenow', progress);
}

function completeTask(taskId, isComplete) {
    const url = '/taskComplete/' + taskId;
    $.ajax({
        url: url,
        type: 'POST',
        data: { complete: isComplete },
        success: function(data, textStatus, jqXHR) {
            console.log('Task status updated successfully');
            updateProgress();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.error('Error updating task status:', errorThrown);
        }
    });
}
function updateCharCount() {
    const textarea = document.getElementById('description');
    const charCount = textarea.value.length;
    document.getElementById('charCount').textContent = charCount;
}
function checkPassword(event, form, projectPassword) {
    if (projectPassword) {
        event.preventDefault();
        var enteredPassword = prompt("Please enter the project password:");
        if (enteredPassword === projectPassword) {
            form.submit();
        } else {
            alert("Incorrect password. Access denied.");
            return false;
        }
    }
    return true;
}
function searchAllProjects() {
    let query = $('#queryAllProjects').val().trim();
    if (query === '') {
        window.location.href = '/home';
    } else {
        $.ajax({
            url: '/home/find',
            type: 'GET',
            data: { query: query , type: "allProjects"},
            success: function(data) {
                console.log('Success function called');
                $('#allProjects').html(data);
            },
            error: function() {
                alert('Error occurred during search');
            }
        });
    }
}

function searchUserProjects() {
    let query = $('#queryUserProjects').val().trim();
    if (query === '') {
        window.location.href = '/home';
    } else {
        $.ajax({
            url: '/home/find',
            type: 'GET',
            data: { query: query , type:"userProjects"},
            success: function(data) {
                console.log('Success function called');
                $('#userProjects').html(data);
            },
            error: function() {
                alert('Error occurred during search');
            }
        });
    }
}

function searchFriendsUsers() {
    let query = $('#queryFriendsUsers').val().trim();
    if (query === '') {
        window.location.href = '/home';
    } else {
        $.ajax({
            url: '/home/find',
            type: 'GET',
            data: {query: query, type: "userFriends"},
            success: function (data) {
                console.log('Success function called');
                $('#usersFriends').html(data);
            },
            error: function () {
                alert('Error occurred during search');
            }
        });
    }
}
function searchAllUsers() {
    let query = $('#queryAllUsers').val().trim();
    if (query === '') {
        window.location.href = '/home';
    } else {
        $.ajax({
            url: '/home/find',
            type: 'GET',
            data: { query: query , type: "allUsers"},
            success: function(data) {
                console.log('Success function called');
                $('#userList').html(data);
            },
            error: function() {
                alert('Error occurred during search');
            }
        });
    }
}
function searchInvitations() {
    let query = $('#queryInvitations').val().trim();
    if (query === '') {
        window.location.href = '/home';
    } else {
        $.ajax({
            url: '/home/find',
            type: 'GET',
            data: { query: query , type: "invitations"},
            success: function(data) {
                console.log('Success function called');
                $('#userFriendsInvList').html(data);
            },
            error: function() {
                alert('Error occurred during search');
            }
        });
    }
}
function confirmDelete() {
    return confirm("Are you sure you want to delete ?");
}