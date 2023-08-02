# Emotes on my Mind

### An Android-StickerApp that allows the creation of WhatsApp-Sticker-Packs ([see requirements](https://github.com/WhatsApp/stickers/tree/main/Android)) with Emotes from [7TV](https://7tv.app/).

# Features & State

### Implemented

- Browse and search Emotes.
- Download, compress and scale static WEBP-Files, so they can be used as WhatsApp-Sticker.
- Download animated WEBP-Files. Not yet available to be used as WhatsApp-Sticker (missing size &
  measurement requirements) but can already be added to animated Sticker-Collections.
- Create Sticker-Collections for static-, and animated Sticker.
- Add/remove Sticker to/from Sticker-Collections.
- Send non-animated Sticker-Collections to WhatsApp.

### Missing / Next up

- Let user filter for Animated, Not Animated (including none and all) in MainFeedScreen.
- Show more data on StickerDetailsScreen and EmoteDetailsScreen.
- Compress and scale animated WEBP-Files, so they can be used as WhatsApp-Sticker. This requires
  taking apart every single frame of the WEBP-File and putting it back together after compressing
  and scaling.
- Remove hardcoded implementation from WhatsApp-Connection and add validation.
- Implement Profile Screen for 7TV-User. The user can choose their 7TV-Username at first ever
  startup and the Profile-Tab will then always represent their own Profile Screen.

# Screens

### Emote Feed

- Browse Emotes on a LazyGrid using pagination to continuously load new Emotes.
- Search for Emotes in SearchBar with locally cached Search-History.
- Clicking on an Emote-Item will bring the user to the either to the StickerDetailsScreen (if
  Sticker has already been downloaded) or the EmoteDetailsScreen (if it hasn't been downloaded yet).

### Emote Details

- Download an Emote as a Sticker. Image-files saved on disk cache will be prioritised. If it can't
  be found locally, the Emote will be downloaded from 7TV again.
- The download is being processed by a CoroutineWorker. It compresses and scales the WEBP-File until
  it matches
  the [requirements for a WhatsApp-Sticker](https://github.com/WhatsApp/stickers/tree/main/Android).
- See Emote-Details from 7TV like size, measurements, creator etc. [TODO]
- After downloading, the user can navigate to the StickerDetailsScreen.

### Sticker Details

- See extra Sticker-Details and Emote-Details. [TODO]
- Open AlertDialog to add/remove Sticker to/from Sticker-Collections.
- Open AlertDialog to delete Sticker. This will delete the Image-File, remove the
  Sticker-Reference from every Sticker-Collection containing this Sticker and then delete the
  Sticker itself.
- Create new Sticker-Collection through another AlertDialog in AddToCollection-AlertDialog.

### Library

- Tabs for Sticker-Collections and Stickers.
- Can filter for Animated, Not Animated (including none and all).
- Clicking on Sticker-Collection will direct the user to the StickerCollectionDetailsScreen.
- Clicking on a Sticker will direct the user to the StickerDetailsScreen.

### Sticker Collection Details

- Display all Sticker in current Collection.
- Send Sticker-Collection to WhatsApp using a ContentProvider via FAB in BottomAppBar.
- Enter EditMode via BottomAppBar to edit the name of the Sticker-Collection or delete it.
- Enter StickerDeleteMode via BottomAppBar where the user can select any Sticker, which will be
  deleted after saving.
- Open a fullscreen AlertDialog via BottomAppBar where the user can select any other already
  downloaded Sticker and add it to the Sticker-Collection after saving.

# Most notable Libraries

- [Jetpack Compose](https://developer.android.com/jetpack/compose) to build the UI.
- [Dagger Hilt](https://dagger.dev/hilt/) for dependency injection.
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) for JSON serialization.
- [Compose Destinations](https://github.com/raamcosta/compose-destinations) to allow for easier
  navigation in Compose with type-safe navigation arguments.
- [MongoDB Realm](https://www.mongodb.com/docs/realm/sdk/kotlin/) for local storage.
- [Coil](https://github.com/coil-kt/coil) for asynchronous loading of images, including memory and
  disk caching.
- [Apollo Kotlin](https://github.com/apollographql/apollo-kotlin) as GraphQL-Client for model
  generation of GraphQL-Queries.
- [Work Manager](https://developer.android.com/jetpack/androidx/releases/work) to reliably run
  tasks. Currently used to handle the process of downloading, compressing and scaling the Emotes to
  become a Sticker.

## Structure

I built the project with a Clean-Architecture approach using Presentation-, Domain-, and
Data-Layers:

- The presentation layer contains the Screens built with Jetpack Compose and their ViewModels.
- The domain layer contains use-cases, which are called from the ViewModels. It also holds the
  general models and repository-interfaces.
- The data layer is used to communicate with data sources like the local database and the remote
  API. This is done by the repository-implementations of the domain layer repository-interfaces. It
  also holds the request-, and response-Models.

The project is separated into one core package and one features package:

- The core package contains shared repositories, UI components and models.
- The features package separates the UI, logic and repositories for each feature.

Features:

- Auth -> currently only a SplashScreen - the plan is to make the user input their username here or
  even connect to 7TV or Twitch in the future to automatically set up their profile. [TODO]
- Emotes -> for the Emotes coming from 7TV.
- Sticker -> for all locally saved Stickers. They are similar but not the same as emotes.
- Profile -> currently empty but will be used for the profile coming from Auth. [TODO]

## Additional Info

This project is still under active development and will be updated whenever I find time for it. It
is meant to show my (Android-) development skills, since all previous projects that I worked on
were proprietary. The development started end of June 2023.  
I plan on eventually releasing it to the PlayStore. I am also very interested in either building
a native iOS-Application-Counterpart or converting it to a cross-platform Application in the future.

Because I already worked with RoomDB before, I chose MongoDB Realm for this project to explore some
new technologies.

[7TV's public API.v3](https://7tv.io/docs) is still under development, which is why I decided to use
their GraphQL-Endpoint instead. The GraphQL-Endpoint is also being used on their Website to load all
the Emotes.

## License

[MIT](https://choosealicense.com/licenses/mit/)