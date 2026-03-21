import {config} from '@vue/test-utils';
import ResizeObserver from 'resize-observer-polyfill';

config.global.renderStubDefaultSlot = true;
config.global.stubs = {
  transition: false,
  'transition-group': false,
  Teleport: false
};

if (typeof globalThis.ResizeObserver === 'undefined') {
  globalThis.ResizeObserver = ResizeObserver;
}
