# ImagePreference

A preference which pulls an image from a local file path and creates a thumbnail icon in the preference.

By default it's set up to choose a photo from the gallery; then, when the image is selected, it's set as the preference value. When the preference is displayed, it will look at that value and try to make a thumbnail from the path. If the path is not found, the default icon is shown instead. The images are cached as SoftReferences, so multiple file loads are avoided unless necessary.

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
