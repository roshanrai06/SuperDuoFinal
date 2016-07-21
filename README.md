# Udacity SuperDuo!


###Welcome to the SuperDuo! project
This repository actually contains 2 projects; the _**Alexandria**_ app that allows for scanning and maintaining a list of books, and the _**FootbalScores**_ app where we can follow soccer matches and their results. This Readme show the release notes of both apps, beginning with the Alexandria app.


##Alexandria 1.1
_**The Alexandria Android app allows for scanning and maintaining a list of books**_

This application retrieves book information from the [Google Books API](https://developers.google.com/books/). All titles, cover images, and author information come from there. The original version of this app was built by Sascha Jaschke, and a modified version is given to students in the Udacity Android Nanodegree program.

|Drawer|Book list|Book detail|Add book|
|---|---|---|---|
| ![Drawer](./Alexandria/doc/screenshots/drawer-2015-11-20-125800.png?raw=true "Drawer") | ![Book list](./Alexandria/doc/screenshots/book-list-2015-11-20-125418.png?raw=true "Book list") |![Book detail](./Alexandria/doc/screenshots/book-detail-2015-11-20-125540.png?raw=true "Book detail")|![Add book](./Alexandria/doc/screenshots/add-book-2015-11-20-125149.png?raw=true "Add book")|

|Scan a new Book|Tablet|
|---|---|
|![Scan](./Alexandria/doc/screenshots/scan-2015-11-20-130602.png?raw=true "Scan")|![Tablet](./Alexandria/doc/screenshots/tablet-2015-11-20-130602.png?raw=true "Tablet")|


###Release notes - v1.1 - 20151101

#####Requirements
Android 4.1 Jelly Bean or later (API level 16)


#####Additions
_The AXX numbers below refer to my [Trello board Alexandria](https://trello.com/b/tcaXsoyg) used for this release, let me know if you want access._

* A35 - Create OmniGraffle schema of v1
* A36 - Refactor and comment where needed
* A23 - Strings are all included in the strings.xml file and untranslatable strings have a translatable tag marked to false
	* Added Portuguese (Brasil) and Dutch (Nederland) translations for all strings
* A08 - Add an image to the book details (apparently there already are cover images, but many books don’t contain an image:
	1. Show a ‘cover not available’ image instead
	2. ~~Do a search in the Open Library API to get the image there~~
	3. Use Glide for loading and caching)
* A09 - Add a thumbnail image to the book list
* A10 - Add a thumbnail image to the Add book/Scan search results
* A17 - Implement a ’toolbar’ in the Settings activity
* A18 - Implement a back button in the ‘toolbar’ of the Settings activity
* A32 - The barcode scanning functionality does not require the installation of a separate app on first use (what does this mean, ‘on first use’?)
* A01 - Implement barcode scanning functionality
* A55 - Check restore state on rotation, etc. (check how it's done in SpotifyStreamer)
* A45 - Restore booklist scrollposition on roation and listitem click to bookdetail
* A51 - When rotating from portrait mode on a tablet in the bookdetail fragment to the landscape mode, left should contain the booklist, and right the previously selected bookdetail fragment
* A54 - Make the old database upgradable by adding the 'saved' field in the 'books' table
* A03 - Hide the keyboard when the side-menu is opened. Mainly on first run of the app after install this is annoying. Alternative to solve this is to not open the side-menu op app-open, but only open the Add/Scan activity
* A04 - On Submit of the Add book field hide the keyboard
* A47 - Change the accent color of the startscreen dialog options to a matching color
* A25 - Add content descriptions for all buttons, images, etc.
* A39 - Check landscape layout
* A38 - Check tablet
* A29 - Add ISBN number to Book detail activity
* A11 - Add the same padding to all activities (About, Book list, Book details, Add/Scan)
* A20 - If there are not yet any books scanned, or they have been deleted, or a search does not return any result, there should appear a message informing the user accordingly. Using the emptyField of the ListView, or just a Toast
* A30 - In the Book List make hitting the Enter button work (now only clicking the search button works)
* A16 - Implement auto-complete for the search field in the Booklist
* A33 - The back button in the toolbar should be a hamburger button when in the Addbook and Booklist activities. It should only be a back button when we are in the Book details screen.
* A06 - When adding a book, on select OK, inform the user that the book was added to the list.
* A36 - Wait with saving the book to the database until the user confirms
* A43 - Save mTitle in the MainActivity to the instanceState on rotation, and get it back onCreate
* A15 - Replace the back button in the book detail page by a back button in the ’toolbar’
* A02 - Check before searching for a book if there is an internet connection, and if not, show a Toast
* A13 - When searching to add a book and nothing is found, shorten the time the Toast is shown
* A42 - Add onClick listener to searchbutton in AddBook to trigger the fetchbook service
* A41 - Add onEnter listener to the searchfield in AddBook to trigger the fetchbook service
* A05 - Separate the Add(typing) and Scan book functions in tabs. One tab for Manual search, one for Scanning. Or another way to separate the submit of manual isbn number typing and scanning
* A37 - Update OmniGraffle class diagram after completing my version


#####Fixes
* A34 - Fix error when rotating the device in Book detail view the app crashes randomly
* A40 - Fix error where AppCompatImageButton cannot cast to Button on rotation
* A07 - Fix the layout issue with the book sub-title in the book details view
* A02 - Check before searching for a book if there is an internet connection, and if not, show a Toast
* A35 - Check existing UnitTests
* A52 - Fixed bug where the toolbar title textview was not updated when navigating back from the main fragments (for example, from booklist back to addbook)
* A49 - Fix bug onrotate in BookFragment where drawer icon is not set correctly to home/back icon
* A48 - Fix layout bugs in v16 devices with left/start and right/end
* A46 - Fix messed-up settings- and sharing-menu's
* A22 - Find extra error cases are found, solve them, and comment them in the code
* A31 - Check if the app requires permissions in the Manifest that are not used
* And many more that I fixed along the way...


###App diagram
![App diagram](./Alexandria/doc/Alexandria.P3.2.png?raw=true "App diagram")


##FootballScores 1.1
_**With the FootballScores app we can follow soccer matches and their results**_


The Football Scores app uses the Football-data.org Api to retrieve the fixtures- and teams data. To be able to use it you need to request an Api Key at: [http://api.football-data.org/register](http://api.football-data.org/register). After you've received the key you need to enter it in the app Settings.

|Main|Detail|Widgets|Settings|
|---|---|---|---|
| ![Main](./FootballScores/doc/screenshots/device-2015-12-07-181150.png?raw=true "Main") | ![Detail](./FootballScores/doc/screenshots/device-2015-12-07-181351.png?raw=true "Detail") |![Widgets](./FootballScores/doc/screenshots/device-2015-12-07-181830.png?raw=true "Widgets")|![Settings](./FootballScores/doc/screenshots/device-2015-12-07-181535.png?raw=true "Settings")|


###Release notes - v1.1 - 20151101


#####Requirements
Android 3.0 Honeycomb or later (API level 11)


#####Additions
_The FSXX numbers below refer to my [Trello board FootballScores](https://trello.com/b/wUOvWbM1) used for this release, let me know if you want access._

* ..


#####Fixes
* ..



###App diagram
![App diagram](./FootballScores/doc/FootballScores.P3.2.png?raw=true "App diagram")








