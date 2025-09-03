// plugins/with-bg-service.js
const { withAndroidManifest } = require('@expo/config-plugins');

module.exports = function withBgService(config) {
  return withAndroidManifest(config, async config => {
    const manifest = config.modResults.manifest;
    // 1) Add the data-sync permission
    manifest['uses-permission'] = manifest['uses-permission'] || [];
    manifest['uses-permission'].push({
      $: { 'android:name': 'android.permission.FOREGROUND_SERVICE_DATA_SYNC' },
    });
    // 2) Add your <service> inside <application>
    const app = manifest.application[0];
    app.service = app.service || [];
    app.service.push({
      $: {
        'android:name': 'com.asterinet.react.bgactions.RNBackgroundActionsTask',
        'android:foregroundServiceType': 'dataSync',
      },
    });

    return config;
  });
};