(function() {
	/**
	 * Variables
	 */
	var user_id = 'abcde';
	var user_fullname = 'abcdefg hijklmn';
	var lon = -122.08;
	var lat = 37.38;
//	
	console.log(user_id);
	
	function init() {
		document.querySelector('#login-form-btn').addEventListener('click', onSessionInvalid);
		document.querySelector('#register-form-btn').addEventListener('click',showRegisterForm);
		document.querySelector('#register-btn').addEventListener('click', register);
		document.querySelector('#login-btn').addEventListener('click', login);
		document.querySelector('#nearby-btn').addEventListener('click',
				loadNearbyItems);
		document.querySelector('#fav-btn').addEventListener('click',
				loadFavoriteItems);
		document.querySelector('#recommend-btn').addEventListener('click',
				loadRecommendedItems);
		validateSession();
	}
	
	function validateSession() {
		onSessionInvalid();
		
		var url = './login';
		var req = JSON.stringify({});
		
		showLoadingMessage('Validating session...');
		
		ajax('GET', url, req, 
				function(res){
					var result = JSON.parse(res);
					if(result.status === 'OK') {
						onSessionValid(result);
					}
				}, function(){
					console.log('login error');
				});
	}
	
	function onSessionInvalid() {
		var loginForm = document.querySelector('#login-form');
		var registerForm = document.querySelector('#register-form');
		var itemNav = document.querySelector('#item-nav');
		var itemList = document.querySelector('#item-list');
		var avatar = document.querySelector('#avatar');
		var welcomeMsg = document.querySelector('#welcome-msg');
		var logoutBtn = document.querySelector('#logout-link');
		
		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(logoutBtn);
		hideElement(welcomeMsg);
		hideElement(registerForm);
		
		clearLoginError();
		showElement(loginForm);
	}
	
	function onSessionValid(result) {
		user_id = result.user_id;
		user_fullname = result.name;
		
		var loginForm = document.querySelector('#login-form');
		var registerForm = document.querySelector('#register-form');
		var itemNav = document.querySelector('#item-nav');
		var itemList = document.querySelector('#item-list');
		var avatar = document.querySelector('#avatar');
		var welcomeMsg = document.querySelector('#welcome-msg');
		var logoutBtn = document.querySelector('#logout-link');
		
		welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;
		
		showElement(itemNav);
		showElement(itemList);
		showElement(avatar);
		showElement(logoutBtn, 'inline-block');
		showElement(welcomeMsg);
		hideElement(loginForm);
		hideElement(registerForm);
		
		initGeoLocation();
	}
	
	function showRegisterForm() {
		var loginForm = document.querySelector('#login-form');
		var registerForm = document.querySelector('#register-form');
		var itemNav = document.querySelector('#item-nav');
		var itemList = document.querySelector('#item-list');
		var avatar = document.querySelector('#avatar');
		var welcomeMsg = document.querySelector('#welcome-msg');
		var logoutBtn = document.querySelector('#logout-link');
		
		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(logoutBtn);
		hideElement(welcomeMsg);
		hideElement(loginForm);
		
		clearRegisterResult();
		showElement(registerForm);
	}
	
	function hideElement(element) {
		element.style.display = 'none';
	}
	
	function clearLoginError() {
		document.querySelector('#login-error').innerHTML = '';
	}
	
	function clearRegisterResult() {
		document.querySelector('#register-result').innerHTML = '';
	}
	
	function showElement(element, style) {
		var displayStyle = style ? style : 'block';
		element.style.display = displayStyle;
	}
	
	function initGeoLocation() {
		if(navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(onPositionUpdated, onLoadPositionFailed, {
				maximumAge : 60000
			});
			showLoadingMessage('Retrieving your location...');
		} else {
			onLoadPositionFailed();
		}
	}
	
	function onPositionUpdated(position) {
		lat = position.coords.latitude;
		lon = position.coords.longitude;
		loadNearbyItems();
	}
	
	function onLoadPositionFailed() {
		console.warn('navigator.geolocation is not available');
		getLocationFromIP();
	}
	
	function getLocationFromIP() {
		var url = 'http://ipinfo.io/json';
		var data = null;
		
		ajax('GET', url, data, 
				function(res){
					var result = JSON.parse(res);
					if('loc' in result) {
						var loc = result.loc.split(',');
						lat = loc[0];
						lon = loc[1];
					} else {
						console.warn('Getting location by IP failed.');
					}
					loadNearbyItems();
				});
	}
	
	function showLoadingMessage(msg) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = '<p classs="notice"><i class="fa fa-spinner fa-spin"></i>'
			                + msg + '</p>';
	}
	
	function showWarningMessage(msg) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> '
				+ msg + '</p>';
	}

	function showErrorMessage(msg) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> '
				+ msg + '</p>';
	}
	
	//----------------------
	// register
	//----------------------
	function register() {
		var username = document.querySelector('#register-username').value;
		var password = document.querySelector('#register-password').value;
		var firstName = document.querySelector('#register-first-name').value;
		var lastName = document.querySelector('#register-last-name').value;
		
		if(username === "" || password === "" || firstName === "" || lastName === "") {
			showRegisterResult('Please fill in all field');
			return;
		}
		
		if(username.match(/^[a-z0-9_]+$/) === null) {
			showRegisterResult('Invalid username');
			return;
		}
		
		password = md5(username + md5(password));
		
		// request parameters
		var url = './register';
		var req = JSON.stringify({
			user_id : username,
			password : password,
			first_name : firstName,
			last_name : lastName,
		});
		
		ajax('POST', url, req, 
		function(res){
			var result = JSON.parse(res);
			if(result.status === 'OK') {
				showRegisterResult('Successfully registered');
			} else {
				showRegisterResult('User already existed');
			}
		}, function(){
			showRegisterResult('Failed to register');
		});
	}
	
	function showRegisterResult(registerMessage) {
		document.querySelector('#register-result').innerHTML = registerMessage;
	}
	function clearRegisterResult() {
		document.querySelector('#register-result').innerHTML = '';
	}

	//----------------------
	// Login
	//----------------------
	function login() {
		var username = document.querySelector('#username').value;
		var password = document.querySelector('#password').value;
		password = md5(username + md5(password));
		
		var url = './login';
		var req = JSON.stringify({
			user_id : username,
			password : password,
		});
		
		ajax('POST', url, req, 
				function(res){
					var result = JSON.parse(res);
					if(result.status === 'OK') {
						onSessionValid(result);
					}
				}, function(){
					showLoginError();
				});
	}
	
	function showLoginError() {
		document.querySelector('#login-error').innerHTML = 'Invalid username or password';
	}
	
	//------------------------------------------------------------
	// load nearby
	//-----------------------------------------------------------
	function loadNearbyItems() {
		console.log('loadNearbyItems');
		activeBtn('nearby-btn');
		
		var url = './search';
		
		var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lon;
		var data = null;
		
		showLoadingMessage('Loading nearby jobs...');
		
		ajax('GET', url + '?' + params, data, 
				function(res){
					var items = JSON.parse(res);
					if(!items || items.length === 0) {
						showWarningMessage('No nearby job.')
					} else {
						listItems(items);
					}
				}, function(){
					showErrorMessage('Cannot load nearby jobs.')
				});
	}
	
	//-------------------------------------------------------------------
	// got list of favorite jobs
	// ------------------------------------------------------------
	/**
	 * API #2 Load favorite (or visited) items API end point: [GET]
	 * /history?user_id=1111
	 */
	function loadFavoriteItems() {
		activeBtn('fav-btn');

		// request parameters
		var url = './history';
		var params = 'user_id=' + user_id;
		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Loading favorite items...');

		// make AJAX call
		ajax('GET', url + '?' + params, req, function(res) {
			var items = JSON.parse(res);
			if (!items || items.length === 0) {
				showWarningMessage('No favorite item.');
			} else {
				listItems(items);
			}
		}, function() {
			showErrorMessage('Cannot load favorite items.');
		});
	}

	//-------------------------------------------------
	// list of recommend jobs
	//----------------------------------------------------------------
	/**
	 * API #3 Load recommended items API end point: [GET]
	 * /recommendation?user_id=1111
	 */
	function loadRecommendedItems() {
		activeBtn('recommend-btn');

		// request parameters
		var url = './recommendation' + '?' + 'user_id=' + user_id + '&lat='
				+ lat + '&lon=' + lon;
		var data = null;

		// display loading message
		showLoadingMessage('Loading recommended items...');

		// make AJAX call
		ajax('GET', url, data,
				// successful callback
				function(res) {
					var items = JSON.parse(res);
					if (!items || items.length === 0) {
						showWarningMessage('No recommended item. Make sure you have favorites.');
					} else {
						listItems(items);
					}
				},
				// failed callback
				function() {
					showErrorMessage('Cannot load recommended items.');
				});
	}

	
	//---------------------------------------
	// favorite set
	//-----------------------------------------
	function changeFavoriteItem(item) {
		// check whether this item has been visited or not
		var li = document.querySelector('#item-' + item.item_id);
		var favIcon = document.querySelector('#fav-icon-' + item.item_id);
		var favorite = !(li.dataset.favorite === 'true');

		// request parameters
		var url = './history';
		var req = JSON.stringify({
			user_id : user_id,
			favorite : item
		});
		var method = favorite ? 'POST' : 'DELETE';

		ajax(method, url, req,
		// successful callback
		function(res) {
			var result = JSON.parse(res);
			if (result.status === 'OK' || result.result === 'SUCCESS') {
				li.dataset.favorite = favorite;
				favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
			}
		}, 
		//error
		function(){
			console.log('change favorite failed!')
		}
		);
	}

	
	/**
	 * helper function that creates a DOM element <tag options...>
	 */
	function create(tag, options) {
		var element = document.createElement(tag);
		for(var key in options) {
			if(options.hasOwnProperty(key)) {
				element[key] = options[key];
			}
		}
		return element;
	}
	
	/**
	 * helper function that makes a navigation button active
	 */
	function activeBtn(btnId) {
		var btns = document.querySelectorAll('.main-nav-btn');
		
		for(var i = 0; i < btns.length; i++) {
			btns[i].className = btns[i].className.replace(/\bactive\b/, '');
		}
		
		var btn = document.querySelector('#' + btnId);
		btn.className += ' active';
	}
	
	//--------------------------------------------------------------
	// creating job list
	// ----------------------------------------------------------------
	/**
	 * list jobs
	 * @param items - array of item json object
	 */
	function listItems(items) {
		var itemList = document.querySelector('#item-list');
		itemList.innerHTML = ''; // clear current results
		
		for(var i = 0; i < items.length; i++) {
			addItem(itemList, items[i]);
		}
	}
	
	/**
	 * Add a single item to the list
	 * 
	 * @param itemList - The <ul id="item-list"> tag (DOM container)
	 * @param item - The item data (JSON object)
	 * <li class="item">
          <img alt="job image" src="https://" />
          <div>
             <a class="item-name" href="#">Job</a>
             <p class="item-category">Senior Software Engineer</p>
             <div class="stars">
                <i class="fa fa-star"></i>
                <i class="fa fa-star"></i>
                <i class="fa fa-star"></i>
             </div>
          </div>
          <p class="item-address">699 Calderon Ave<br />Mountain View<br /> CA</p>
          <div class="fav-link">
             <i class="fa fa-heart"></i>
          </div>
        </li>
	 */
	function addItem(itemList, item) {
		var item_id = item.item_id;
		
		var li = create('li', {
			id : 'item-' + item_id,
			className : 'item'
		});
		
		li.dataset.item_id = item_id;
		li.dataset.favorite = item.favorite;
		
		if (item.image_url) {
			li.appendChild(create('img', {
				src : item.image_url
			}));
		} else {
			li.appendChild(create('img', {
				src : 'https://via.placeholder.com/100'
			}));
		}
		// section
		var section = create('div');

		// title
		var title = create('a', {
			className : 'item-name',
			href : item.url,
			target : '_blank'
		});
		title.innerHTML = item.name;
		section.appendChild(title);

		// keyword
		var keyword = create('p', {
			className : 'item-keyword'
		});
		keyword.innerHTML = 'Keyword: ' + item.keywords.join(', ');
		section.appendChild(keyword);

		li.appendChild(section);

		// address
		var address = create('p', {
			className : 'item-address'
		});

		// ',' => '<br/>', '\"' => ''
		address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g,
				'');
		li.appendChild(address);

		// favorite link
		var favLink = create('p', {
			className : 'fav-link'
		});

		favLink.onclick = function() {
			changeFavoriteItem(item);
		};

		favLink.appendChild(create('i', {
			id : 'fav-icon-' + item_id,
			className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
		}));

		li.appendChild(favLink);
		itemList.appendChild(li);

	}
	
	/**
	 * this function handling the ajax request
	 * @param method - http request type
	 * @param url - requesting target url
	 * @param data - the body of http request
	 * @param successCallback 
	 * @param errorCallback
	 */
	function ajax(method, url, data, successCallback, errorCallback) {
		var xhr = new XMLHttpRequest();
		
		xhr.open(method, url, true);
		
		xhr.onload = function() {
			if(xhr.status === 200) {
				successCallback(xhr.responseText);
			} else {
				errorCallback();
			}
		};
		
		xhr.onerror = function() {
			console.error("The request couldn't be completed.");
			errorCallback();
		}
		
		if (data === null) {
			xhr.send();
		} else {
			xhr.setRequestHeader("Content-Type",
					"application/json;charset=utf-8");
			xhr.send(data);
		}

	}
	
	init();
})();


