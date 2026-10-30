// Polyfill for Node runtimes lacking zstd stream decompression (prevents undici decompress.js crash)
import zlib from 'node:zlib';
if (!(zlib as any).constants) {
  (zlib as any).constants = {};
}
if ((zlib as any).constants.ZSTD_d_windowLogMax === undefined) {
  (zlib as any).constants.ZSTD_d_windowLogMax = 23;
}
if (typeof (zlib as any).createZstdDecompress !== 'function') {
  (zlib as any).createZstdDecompress = function () {
    return new (require('node:stream').PassThrough)();
  };
}

import '@testing-library/jest-dom';
