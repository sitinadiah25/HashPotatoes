# HashPotatoes
### Orbital Project 2019

### Team Members
@nadiah25 
@janicetyy

## Project Overview

###Proposed Level of Achievement
Project Gemini

### Motivation
Students in NUS can decide which modules to take up each semester and depending on their own pace, which may differ from their existing peers. Hence, it is difficult to create a new social circle where they are able to carry out effective discussion for each modules quickly. Some may even fear that their questions are too simple, where they may then appear to be less competent. In addition, existing school forums are web-based thus less convenient to access through mobile devices, and notifications to alert about responses to their questions are not available.

We want to create an application where all students can create discussions freely through anonymity, so that everyone can view and learn from each discussion, regardless of whether they have a large social circle in the same module. The application would be built on android platform allowing students to receive notifications for their own posts and tags that that they express interests in, be it module-related or random sharings. Students can also view discussions posted by others to find out perspective that they might not have thought of. 

### Aim
Through this application, we hope that everyone can have a better learning environment by encouraging and amplifying the benefit of having study discussions.

### Technology stack
* Android Studio (Java)
* Firebase
* Version Control System (GitHub)

### Features
#### Implemented Features
* LOGIN PAGE - This page will authenticate user identity, currently by email and password. Users cannot use the application unless they have an account. [This is implemented so that prototype submitted is usuable even if change to NUSNET ID is unavailable.]
* REGISTRATION PAGE - This page will allow user to create user account, requesting for email and password. Other details can be changed within the application.
* HOME FEED PAGE - This page will display 10 most recent posts that user has followed/favourited. 
    * CREATING POST BUTTON - This button if located on the top right corner of the home feed. On click, this button will bring user to post creation where user can create a new discussiona, search for relevant tags and determine anonymity.
* FEATURED PAGE / SEARCH FEATURE - This page will have a search bar where users can search for tags that they are interested in.
   * FOLLOW/FAVOURITE SYSTEM - Users can follow tags they are interested in and unfollow tags they are no longer interested in. Posts with followed tags will show u in home feed page.
* TAGGING SYSTEM - This system is to help user find posts that could be relevant to what they are searching for.
    * PRIVATE - Each tag will have a whitelist where the owner can add users in it and determine their access rights. These tags are pre-created in profile page.
    * PUBLIC - Anyone can see these posts, all tags are set to this by default 
* OWN POST PAGE / PROFILE - Users can only see their own profile, all their posts, information and personal settings are available here.
    * EDIT PROFILE - This page is for general change of personal information such as username or email.
* COMMENT AND LIKE SYSTEM - USers can comment on the posts and like the post if they feel that a post is helpful to them.

####New Features planned for milestone 3 / Bugs to be fixed / Features to be improved
* LOGIN PAGE - This is planned for change to NUSNET ID, email and password will be hidden or removed if needed.
* REGISTRATION PAGE - If the above feature is functional, this page may be hidden or removed as well.
* HOME FEED - Upon reaching the end of the page, another 10 posts should be loaded until all posts have been viewed.
    * CREATING POST - Users can only chose from existing tags, if no existing tag is relevant, user must create one on their own.
* FEATURED PAGE / SEARCH FEATURE - Search feature is exact match, elastic search may be explored for more efficient searching.
* TAGGING SYSTEM - Whitelistinf of authorized users for private tags.
* OWN POST PAGE / PROFILE
    * EDIT AND DELETE POST - Users can remove or/and make changes to their own posts. Confirmation message dialog should pop up to confirm user action.
* BADGE FEATURE - Suggested by out peer evaluator, this feature will identify between student and lecturers so that users can better determine the validity of comments.
* NOTIFICATION FEATURE - This feature will allow user to be notified if there are new comments or likes on their post. Owneres of private tags will also receive notfication for new follower request.
* REPORT FEATURE - This feature should allow other users to report misuse of anonymity through inppropriate posts/comments.

##### For more bugs, please refer to this link:https://docs.google.com/spreadsheets/d/1SvC7wvhWR5oNn7IT9efshCH_Tthl4medI6pe2t1cDqk/edit?usp=sharing

####Features removed from project planned
Features removed for milestone 3 are as listed below, we decided to remove them as they are not of high priority and we feel that other features plays a more important role in our application. 
* Module planner
* Personal project page

####Problems encountered
* New bugs all the time
* Obsolete code in tutorials


