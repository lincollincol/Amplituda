#!/usr/bin/env bash

case $ANDROID_ABI in
  x86)
    # Disable asm for x86 32-bit due to text relocations
    EXTRA_BUILD_CONFIGURATION_FLAGS+=" --disable-asm"
    ;;
  x86_64)
    EXTRA_BUILD_CONFIGURATION_FLAGS+=" --x86asmexe=${FAM_YASM}"
    ;;
esac

# Enable requested external libraries
ADDITIONAL_COMPONENTS=
for LIBARY_NAME in ${FFMPEG_EXTERNAL_LIBRARIES[@]:-}
do
  ADDITIONAL_COMPONENTS+=" --enable-$LIBARY_NAME"
done

# Dependency include/lib paths
DEP_CFLAGS="-I${BUILD_DIR_EXTERNAL}/${ANDROID_ABI}/include"
DEP_LD_FLAGS="-L${BUILD_DIR_EXTERNAL}/${ANDROID_ABI}/lib ${FFMPEG_EXTRA_LD_FLAGS:-}"

# 16 KB page size support
PAGE_LD_FLAGS="-Wl,-z,max-page-size=16384"

./configure \
  --prefix=${BUILD_DIR_FFMPEG}/${ANDROID_ABI} \
  --enable-cross-compile \
  --target-os=android \
  --arch=${TARGET_TRIPLE_MACHINE_BINUTILS} \
  --sysroot=${SYSROOT_PATH} \
  --cc=${FAM_CC} \
  --cxx=${FAM_CXX} \
  --ld=${FAM_LD} \
  --ar=${FAM_AR} \
  --as=${FAM_CC} \
  --nm=${FAM_NM} \
  --ranlib=${FAM_RANLIB} \
  --strip=${FAM_STRIP} \
  --extra-cflags="-O3 -fPIC ${DEP_CFLAGS} ${EXTRA_CFLAGS:-}" \
  --extra-ldflags="${DEP_LD_FLAGS} ${PAGE_LD_FLAGS}" \
  --enable-shared \
  --disable-static \
  --pkg-config=${PKG_CONFIG_EXECUTABLE} \
  ${EXTRA_BUILD_CONFIGURATION_FLAGS} \
  --disable-programs \
  --disable-muxers \
  --disable-gray \
  --disable-avdevice \
  --disable-swscale \
  --disable-postproc \
  --disable-doc \
  --disable-debug \
  --disable-network \
  --disable-pixelutils \
  --disable-avfilter \
  --disable-encoders \
  --disable-decoders \
  --disable-ffprobe \
  --disable-v4l2-m2m \
  --disable-vaapi \
  --disable-vdpau \
  --disable-videotoolbox \
  --disable-d3d11va \
  --disable-dxva2 \
  --disable-ffnvcodec \
  --disable-nvdec \
  --disable-nvenc \
  --disable-bsfs \
  --disable-autodetect \
  --disable-outdevs \
  --disable-hwaccels \
  --disable-gpl \
  \
  --enable-avformat \
  --enable-avcodec \
  --enable-demuxers \
  --enable-parsers \
  --enable-runtime-cpudetect \
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
  --build-suffix=-amplituda \
  $ADDITIONAL_COMPONENTS || exit 1

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
