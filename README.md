# EasyPermissions

[![](https://jitpack.io/v/Caij/EasyPermissions.svg)](https://jitpack.io/#Caij/EasyPermissions)

Android M permission model.

## Setup

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Caij:EasyPermissions:2.0.0'
}
```

## Usage

```java
 EasyPermissions.requestPermissions(MainActivity.this, 100, new PermissionCallback() {
                    @Override
                    public void onPermissionsGranted(int requestCode, List<String> permissions) {

                    }

                    @Override
                    public void onPermissionsDenied(int requestCode, List<String> permissions) {

                    }

                    @Override
                    public void onNeverAskAgainPermission(int requestCode, List<String> permissions) {

                    }
                }, perms);
```

# License

```
Copyright (C) 2018 Caij

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```