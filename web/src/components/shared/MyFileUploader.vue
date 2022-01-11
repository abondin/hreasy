<!-- Simple file uploader based on
https://github.com/lian-yue/vue-upload-component/blob/master/docs/views/examples/Drag.vue
example
-->
<template>
  <div class="upload-area">
    <v-chip v-if="files && files.length>0">{{ files[0].name }}
      <v-icon v-if="files.length>0 && files[0].error" @click="$refs.upload.clear()">mdi-close</v-icon>
    </v-chip>
    <p v-else class="text-center text--darken-4">{{ $t('Выберите файл на вашем компьютере или перетащите мышкой') }}</p>
    <file-upload
        class="upload-btn"
        v-model="files"
        :drop="true"
        :multiple="false"
        :size="maximumSize"
        :timeout="timeout"
        :post-action="postAction"
        @input="$refs.upload.active=true"
        @close="close"
        ref="upload">
      <v-btn class="primary" v-if="$refs.upload && !$refs.upload.dropActive && (!files || files.length==0)">
        {{ $t('Выбрать')}}
      </v-btn>
    </file-upload>
    <v-alert v-if="files.length>0 && !files[0].error  && !files[0].success">{{ files[0].progress }} %</v-alert>
    <v-alert class="success" v-if="files.length>0 && files[0].success">{{ $t('Файл успешно загружен') }}</v-alert>
    <v-alert class="error" v-if="files.length>0 && files[0].error">
      {{ $t('UPLOAD_ERROR_' + files[0].error, {timeout: timeout, maximumSize: maximumSize}) }}
    </v-alert>
    <div v-if="$refs.upload && !$refs.upload.dropActive">
      <v-btn text @click="close">{{ (files.length > 0 && files[0].success) ? $t('ОK') : $t('Отмена') }}</v-btn>
    </div>

    <div v-if="$refs.upload && $refs.upload.dropActive" class="drop-active"><h1>
      {{ $t('Перетащите файл в данную область') }}</h1></div>
  </div>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import VueUploadComponent from 'vue-upload-component'
import {Prop} from "vue-property-decorator";


@Component({
  components: {'file-upload': VueUploadComponent}
})
export default class MyFileUploader extends Vue {
  // TODO Fix the issue
  // eslint-disable-next-line no-undef
  files: VUFile[] = [];

  @Prop({required: true})
  postAction!: string

  /**
   * 10 * 1024 * 1024 = 10МБ
   * @private
   */
  @Prop({required: false, default: 10485760})
  maximumSize!: number;

  /**
   * 30 * 1000 = 30seconds
   * @private
   */
  @Prop({required: false, default: 30000})
  timeout!: number;

  private close() {
    const event: UploadCompleteEvent = {
      uploaded: (this.files.length > 0 && this.files[0].success) ? true : false
    };
    (this.$refs.upload as VueUploadComponent).clear();
    this.$emit('close', event);
  }

}

export interface UploadCompleteEvent {
  uploaded: boolean
}

</script>


<style lang="css">

.upload-area {
  min-height: 220px;
  position: relative;
  border: grey 2px dashed;
  border-radius: 5px;
  text-align: center;
  vertical-align: middle;
  padding-top: 50px;
  padding-bottom: 50px;
  background-color: white;
}

.upload-btn {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.upload-area .drop-active {
  vertical-align: middle;
  position: absolute;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  padding-top: 50px;
  background-color: grey;
}

</style>
