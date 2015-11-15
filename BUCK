prebuilt_native_library(
  name = 'tfs_sdk_native_libs',
  native_libs = 'lib/native',
)

prebuilt_jar(
  name = 'tfs_sdk_lib',
  binary_jar = 'lib/com.microsoft.tfs.sdk-11.0.0.jar',
)

gerrit_plugin(
  name = 'its-tfs',
  srcs = glob(['src/main/java/**/*.java']),
  manifest_entries = [
    'Gerrit-Module: com.googlesource.gerrit.plugins.its.tfs.TfsModule',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.its.tfs.InitTfs',
    'Gerrit-ReloadMode: reload',
    'Implementation-Title: Team Foundation Server ITS Plugin',
    'Implementation-URL: https://github.com/bjoernrennfanz/gerrit-its-tfs',
    'Implementation-Vendor: Björn Rennfanz',
  ],
  deps = [
    ':tfs_sdk_lib',
    ':tfs_sdk_native_libs',
    '//plugins/its-base:its-base__plugin',
  ],
)
