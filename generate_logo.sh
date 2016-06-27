#!/usr/bin/env bash

set -euo pipefail

BASE_PATH='app/src/main/res'

convert -background '#3F51B5' -fill '#eee' -font Arial -size 192x192 -gravity center -border 15x15 -bordercolor '#3F51B5' label:VD logo.png
convert logo.png \( +clone -alpha extract -draw 'fill black polygon 0,0 0,15 15,0 fill white circle 15,15 15,0' \( +clone -flip \) -compose Multiply -composite \( +clone -flop \) -compose Multiply -composite \) -alpha off -compose CopyOpacity -composite logo.png
cp logo.png $BASE_PATH/mipmap-xxxhdpi/ic_launcher.png
convert logo.png -resize 144x144 $BASE_PATH/mipmap-xxhdpi/ic_launcher.png
convert logo.png -resize 96x96 $BASE_PATH/mipmap-xhdpi/ic_launcher.png
convert logo.png -resize 72x72 $BASE_PATH/mipmap-hdpi/ic_launcher.png
convert logo.png -resize 48x48 $BASE_PATH/mipmap-mdpi/ic_launcher.png
rm logo.png

