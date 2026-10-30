const zlib = require('node:zlib');
if (!zlib.constants) {
  zlib.constants = {};
}
if (zlib.constants.ZSTD_d_windowLogMax === undefined) {
  zlib.constants.ZSTD_d_windowLogMax = 23;
}
if (typeof zlib.createZstdDecompress !== 'function') {
  zlib.createZstdDecompress = function () {
    return new (require('node:stream').PassThrough)();
  };
}

