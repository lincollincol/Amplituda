#!/usr/bin/env bash

case $ANDROID_ABI in
  x86)
    # Disabling assembler optimizations, because they have text relocations
    EXTRA_BUILD_CONFIGURATION_FLAGS="$EXTRA_BUILD_CONFIGURATION_FLAGS --disable-asm"
    ;;
  x86_64)
    EXTRA_BUILD_CONFIGURATION_FLAGS="$EXTRA_BUILD_CONFIGURATION_FLAGS --x86asmexe=${FAM_YASM}"
    ;;
esac

# Preparing flags for enabling requested libraries
ADDITIONAL_COMPONENTS=
for LIBRARY_NAME in ${FFMPEG_EXTERNAL_LIBRARIES[@]}
do
  ADDITIONAL_COMPONENTS+=" --enable-$LIBRARY_NAME"
done

# Referencing dependencies without pkgconfig
DEP_CFLAGS="-I${BUILD_DIR_EXTERNAL}/${ANDROID_ABI}/include"
DEP_LD_FLAGS="-L${BUILD_DIR_EXTERNAL}/${ANDROID_ABI}/lib $FFMPEG_EXTRA_LD_FLAGS"

# Android 15 with 16 kb page size support
# https://developer.android.com/guide/practices/page-sizes#compile-r27
EXTRA_LDFLAGS="-Wl,-z,max-page-size=16384 $DEP_LD_FLAGS"

./configure \
  --prefix=${BUILD_DIR_FFMPEG}/${ANDROID_ABI} \
  --enable-cross-compile \
  --target-os=android \
  --arch=${TARGET_TRIPLE_MACHINE_ARCH} \
  --sysroot=${SYSROOT_PATH} \
  --cc=${FAM_CC} \
  --cxx=${FAM_CXX} \
  --ld=${FAM_LD} \
  --ar=${FAM_AR} \
  --as=${FAM_CC} \
  --nm=${FAM_NM} \
  --ranlib=${FAM_RANLIB} \
  --strip=${FAM_STRIP} \
  --extra-cflags="-O3 -fPIC $DEP_CFLAGS" \
  --extra-ldflags="$EXTRA_LDFLAGS" \
  --enable-shared \
  --enable-network \
  --disable-static \
  --disable-vulkan \
  --disable-gpl \
  --build-suffix=-amplituda \
  --pkg-config=${PKG_CONFIG_EXECUTABLE} \
  \
  --enable-avutil \
  --enable-avformat \
  --enable-avcodec \
  --enable-swresample \
  \
  --disable-programs \
  --disable-ffmpeg \
  --disable-ffplay \
  --disable-ffprobe \
  \
  --disable-avfilter \
  --disable-postproc \
  --disable-vulkan \
  --disable-swscale \
  \
  --disable-gray \
  --disable-debug \
  --disable-pixelutils \
  --disable-v4l2-m2m \
  --disable-vaapi \
  --disable-vdpau \
  --disable-videotoolbox \
  --disable-d3d11va \
  --disable-dxva2 \
  --disable-ffnvcodec \
  --disable-nvdec \
  --disable-nvenc \
  \
  --disable-encoders \
  --disable-decoders \
  --disable-muxers \
  --disable-filters \
  --disable-bsfs \
  \
  --enable-demuxers \
  --enable-parsers \
  \
  --enable-decoder=mp1 \
  --enable-decoder=mp1float \
  --enable-decoder=mp2 \
  --enable-decoder=mp2float \
  --enable-decoder=mp3 \
  --enable-decoder=mp3float \
  --enable-decoder=mp3adu \
  --enable-decoder=mp3adufloat \
  --enable-decoder=mp3on4 \
  --enable-decoder=mp3on4float \
  --enable-decoder=opus \
  --enable-decoder=libopus \
  --enable-decoder=paf_audio \
  --enable-decoder=pcm_alaw \
  --enable-decoder=pcm_bluray \
  --enable-decoder=pcm_dvd \
  --enable-decoder=pcm_f16le \
  --enable-decoder=pcm_f24le \
  --enable-decoder=pcm_f32be \
  --enable-decoder=pcm_f32le \
  --enable-decoder=pcm_f64be \
  --enable-decoder=pcm_f64le \
  --enable-decoder=pcm_lxf \
  --enable-decoder=pcm_mulaw \
  --enable-decoder=pcm_s16be \
  --enable-decoder=pcm_s16be_planar \
  --enable-decoder=pcm_s16le \
  --enable-decoder=pcm_s16le_planar \
  --enable-decoder=pcm_s24be \
  --enable-decoder=pcm_s24daud \
  --enable-decoder=pcm_s24le \
  --enable-decoder=pcm_s24le_planar \
  --enable-decoder=pcm_s32be \
  --enable-decoder=pcm_s32le \
  --enable-decoder=pcm_s32le_planar \
  --enable-decoder=pcm_s64be \
  --enable-decoder=pcm_s64le \
  --enable-decoder=pcm_s8 \
  --enable-decoder=pcm_s8_planar \
  --enable-decoder=pcm_u16be \
  --enable-decoder=pcm_u16le \
  --enable-decoder=pcm_u24be \
  --enable-decoder=pcm_u24le \
  --enable-decoder=pcm_u32be \
  --enable-decoder=pcm_u32le \
  --enable-decoder=pcm_u8 \
  --enable-decoder=pcm_zork \
  --enable-decoder=aac \
  --enable-decoder=aac_fixed \
  --enable-decoder=aac_latm \
  --enable-decoder=ac3 \
  --enable-decoder=ac3_fixed \
  --enable-decoder=flac \
  --enable-decoder=gsm \
  --enable-decoder=libgsm \
  --enable-decoder=gsm_ms \
  --enable-decoder=libgsm_ms \
  --enable-decoder=vorbis \
  --enable-decoder=libvorbis \
  --enable-decoder=wavesynth \
  --enable-decoder=wavpack \
  --enable-decoder=ws_snd1 \
  --enable-decoder=wmalossless \
  --enable-decoder=wmapro \
  --enable-decoder=wmav1 \
  --enable-decoder=wmav2 \
  --enable-decoder=amrnb \
  --enable-decoder=amrwb \
  --enable-decoder=als \
  \
  --disable-hwaccels \
  --disable-avdevice \
  --disable-doc \
  --disable-htmlpages \
  --disable-manpages \
  --disable-podpages \
  --disable-txtpages \
  \
  ${EXTRA_BUILD_CONFIGURATION_FLAGS} \
  $ADDITIONAL_COMPONENTS || exit 1

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install