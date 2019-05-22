# HashPotatoes
### Orbital Project 2019

### Team Members
@nadiah25 
@janicetyy

## Project Overview

### Motivation
Students in NUS can decide which modules to take up each semester and depending on their own pace, which may differ from their existing peers. Hence, it is difficult to create a new social circle where they are able to carry out effective discussion for each modules quickly. Some may even fear that their questions are too simple, where they may then appear to be less competent. In addition, existing school forums are web-based thus less convenient to access through mobile devices, and notifications to alert about responses to their questions are not available.

We want to create an application where all students can create discussions freely through anonymity, so that everyone can view and learn from each discussion, regardless of whether they have a large social circle in the same module. The application would be built on android platform allowing students to receive notifications for their own posts and tags that that they express interests in, be it module-related or random sharings. Students can also view discussions posted by others to find out perspective that they might not have thought of. 

### Aim
Through this application, we hope that everyone can have a better learning environment by encouraging and amplifying the benefit of having study discussions.

### Features
#### Description of the product
* LOGIN PAGE - This page will authenticate user identity, currently by email and password, may integrate with other platforms later
* REGISTRATION PAGE - This page will allow user to create user account, requesting for email, password and other details to be determined later. (Will update again)
* CREATING POST BUTTON - On click, this button will bring user to post creation where user can create a new discussiona, add tags and determine anonymity.
* TAGGING SYSTEM - This system will  determine visibility of posts. Only one tag can be assigned to each posts to prevent conflict temporarily.
    * PRIVATE - Each tag will have a whitelist where the owner can add users in it and determine their access rights. These tags are pre-created in profile page.
    * PUBLIC - Anyone can see these posts, all tags are set to this by default 
* HOME FEED - This page will display all posts that user has followed/favourited.
    * FOLLOW/FAVOURITE SYSTEM - Users can follow tags that they are interested in so that they see new related posts quickly on their feed
* OWN POST PAGE / PROFILE - For now, users cannot see each other's profile.
    * EDIT AND DELETE POST OPTION - Users can see all the post they own which are not available to the others.
* SEARCH BAR - Users can search through keywords or tags for posts that may be of interest
* SETTINGS - This page is for general application settings, such as change of password or notification settings.

### Technology stack
* Android Studio (Java)
* Firebase
* Version Control System (GitHub)
