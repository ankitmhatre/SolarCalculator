# SolarCalculator
Solar Calculator to assist Photographers

## What we built
One of the crucial elements of pursuing Photography is understanding to use Earth's natural light sources ( *Sun and Moon* ) to our advantage. A trick to naturally take better photographs is to shoot in the [Golden Hour](https://en.wikipedia.org/wiki/Golden_hour_(photography)). As the name suggests this begins approximately an hour before sunset. 

In order to assist Photographers with planning of their photoshoots, we built an Application that provides Rising & Setting time of Sun and the Moon. **Rising & Setting time will be referred to as _Phasetime_ hereafter**. 

**Phasetime** can be calculated using this [Algorithm](https://web.archive.org/web/20161202180207/http://williams.best.vwh.net/sunrise_sunset_algorithm.htm). Implementation requires a date and location ( _longitude and lattitude_ ) as an input. User can provide the desired location using a Search Bar, move the Red pin or the Application will use current GPS location as default. For quick access, Application will be able to show past persisted locations by the user.


## Mockup
![Solar Calculator](https://i.imgur.com/cSeNZga.png)

Provided Mockup is a sample representation of the Application Interface.

## Guidelines
*  In the strings.xml make sure to replace YOUR_GOOGLE_MAPS_API_KEY with your own Google Maps API Key which can be obtained [here](https://developers.google.com/maps/documentation/javascript/get-api-key) 
*  [Signed APK](https://developer.android.com/studio/publish/app-signing) It is present in the release directory.


## TODO's
* Plot lines ( As shown in Mockup ) according to Rising / Setting directions of Sun and the Moon.
* We do hav the Azimuth and Elevation, We soon will add Polylines to the MapView and paint it with the celestial rise and set location path.
