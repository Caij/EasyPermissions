# EasyPermissions

[![](https://jitpack.io/v/Caij/EasyPermissions.svg)](https://jitpack.io/#Caij/EasyPermissions)

makes Android runtime permission request extremely easy

## Setup

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Caij:EasyPermissions:last.release.version'
}
```

## Usage

```java
Permissions.with(this)
                .permissions(perms)
                .showReasonBeforeRequest()
                .request(new PermissionListener() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted, @NonNull List<String> grantResults, @NonNull List<String> deniedResults) {
                        if (allGranted) {
                            Toast.makeText(MainActivity.this, "同意", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "拒绝", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
