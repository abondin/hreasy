import {describe, it, expect, beforeEach, afterEach, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {defineComponent, h, nextTick} from 'vue';
import ProfileAvatar from '@/views/profile/components/ProfileAvatar.vue';
import type {WithAvatar} from '@/services/employee.service';
const mockGetEmployeeAvatarUrl = vi.hoisted(() => vi.fn<(id: number) => string>());
const mockUploadEmployeeAvatar = vi.hoisted(() => vi.fn());
const mockHasPermission = vi.hoisted(() => vi.fn<(permission: string) => boolean>());

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: {n?: unknown}) =>
      typeof params?.n !== 'undefined' ? `${key}:${params.n}` : key
  })
}));

vi.mock('@/services/employee.service', () => ({
  getEmployeeAvatarUrl: mockGetEmployeeAvatarUrl,
  uploadEmployeeAvatar: mockUploadEmployeeAvatar
}));

vi.mock('@/lib/permissions', () => ({
  Permissions: {
    UpdateAvatar: 'update_avatar'
  },
  usePermissions: () => ({
    hasPermission: mockHasPermission
  })
}));

const OverlayStub = defineComponent({
  name: 'VOverlayStub',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue'],
  setup(props, {slots}) {
    return () =>
      props.modelValue
        ? h('div', {class: 'v-overlay-stub', 'data-test': 'avatar-overlay'}, slots.default?.())
        : null;
  }
});

const DialogStub = defineComponent({
  name: 'VDialogStub',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue'],
  setup(props, {slots}) {
    return () =>
      props.modelValue
        ? h('div', {class: 'v-dialog-stub'}, slots.default?.())
        : null;
  }
});

const AvatarStub = defineComponent({
  name: 'VAvatarStub',
  emits: ['click'],
  setup(_, {slots, emit, attrs}) {
    return () =>
      h(
        'button',
        {
          ...attrs,
          type: 'button',
          class: ['v-avatar-stub', attrs.class],
          'data-test': 'avatar-toggle',
          onClick: (event: Event) => emit('click', event)
        },
        slots.default?.()
      );
  }
});

const ButtonStub = defineComponent({
  name: 'VBtnStub',
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click'],
  setup(props, {slots, emit, attrs}) {
    return () =>
      h(
        'button',
        {
          ...attrs,
          type: 'button',
          class: ['v-btn-stub', attrs.class],
          'data-test': 'upload-btn',
          disabled: props.disabled || props.loading,
          onClick: (event: Event) => {
            if (!props.disabled && !props.loading) {
              emit('click', event);
            }
          }
        },
        slots.default?.()
      );
  }
});

const ImageStub = defineComponent({
  name: 'VImgStub',
  props: {
    src: {
      type: String,
      default: ''
    },
    alt: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    return () => h('img', {class: 'v-img-stub', src: props.src, alt: props.alt});
  }
});

const simpleStub = (name: string) =>
  defineComponent({
    name: `${name}Stub`,
    setup(_, {slots, attrs}) {
      return () =>
        h(
          'div',
          {
            ...attrs,
            class: [`${name}-stub`, attrs.class]
          },
          slots.default?.()
        );
    }
  });

function mountProfileAvatar(ownerOverride?: Partial<WithAvatar>, readOnly = true) {
  const owner: WithAvatar = {
    id: 42,
    displayName: 'John Doe',
    hasAvatar: true,
    ...ownerOverride
  };

  return mount(ProfileAvatar, {
    props: {
      owner,
      readOnly
    },
    global: {
      stubs: {
        transition: false,
        'transition-group': false,
        VOverlay: OverlayStub,
        VDialog: DialogStub,
        VAvatar: AvatarStub,
        VBtn: ButtonStub,
        VImg: ImageStub,
        VCard: simpleStub('v-card'),
        VCardText: simpleStub('v-card-text'),
        VCardTitle: simpleStub('v-card-title'),
        VCardActions: simpleStub('v-card-actions'),
        VIcon: simpleStub('v-icon'),
        VSpacer: simpleStub('v-spacer'),
        VSlider: simpleStub('v-slider')
      }
    }
  });
}

describe('ProfileAvatar', () => {
  beforeEach(() => {
    mockGetEmployeeAvatarUrl.mockReset();
    mockGetEmployeeAvatarUrl.mockImplementation(id => `https://cdn.test/avatar/${id}`);
    mockUploadEmployeeAvatar.mockReset();
    mockHasPermission.mockReset();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders avatar image when owner has avatar', () => {
    mockHasPermission.mockReturnValue(false);
    const wrapper = mountProfileAvatar();

    const avatarImg = wrapper.find('img');
    expect(avatarImg.exists()).toBe(true);
    expect(avatarImg.attributes('src')).toBe('https://cdn.test/avatar/42');
  });

  it('opens preview overlay when avatar is clicked', async () => {
    mockHasPermission.mockReturnValue(false);
    const wrapper = mountProfileAvatar();

    expect(wrapper.find('[data-test="avatar-overlay"]').exists()).toBe(false);
    await wrapper.find('[data-test="avatar-toggle"]').trigger('click');
    await nextTick();

    expect(wrapper.find('[data-test="avatar-overlay"]').exists()).toBe(true);
  });

  it('disables avatar change in read-only mode even with permission', () => {
    mockHasPermission.mockReturnValue(true);
    const wrapper = mountProfileAvatar({}, true);

    expect(wrapper.find('[data-test="upload-btn"]').exists()).toBe(false);
    expect(wrapper.find('input[type="file"]').exists()).toBe(false);
  });

  it('hides upload controls without permission', () => {
    mockHasPermission.mockReturnValue(false);
    const wrapper = mountProfileAvatar({}, false);

    expect(wrapper.find('[data-test="upload-btn"]').exists()).toBe(false);
    expect(wrapper.find('input[type="file"]').exists()).toBe(false);
  });

  it('allows avatar upload when readOnly is false and permission granted', async () => {
    mockHasPermission.mockReturnValue(true);
    const wrapper = mountProfileAvatar({hasAvatar: false}, false);

    const uploadBtn = wrapper.find('[data-test="upload-btn"]');
    expect(uploadBtn.exists()).toBe(true);
    expect(uploadBtn.text()).toContain('Загрузить фото');

    const input = wrapper.find('input[type="file"]');
    const clickSpy = vi.spyOn(input.element as HTMLInputElement, 'click');
    await uploadBtn.trigger('click');
    expect(clickSpy).toHaveBeenCalled();
  });

  it('displays error message for unsupported files', async () => {
    mockHasPermission.mockReturnValue(true);
    const wrapper = mountProfileAvatar({hasAvatar: false}, false);

    const input = wrapper.find('input[type="file"]');
    const file = new File(['not-image'], 'document.txt', {type: 'text/plain'});
    Object.defineProperty(input.element, 'files', {
      value: [file],
      configurable: true
    });

    await input.trigger('change');
    await nextTick();

    expect(wrapper.text()).toContain('Поддерживаются только изображения');
  });
});
