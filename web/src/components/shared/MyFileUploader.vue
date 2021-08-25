<!-- Simple file uploader based on
https://github.com/lian-yue/vue-upload-component/blob/master/docs/views/examples/Drag.vue
example
-->
<template>
  <div class="upload-area">
    <v-chip v-if="files && files.length>0">{{ files[0].name }}</v-chip>
    <file-upload
        class="upload-btn"
        v-model="files"
        :drop="true"
        :multiple="false"
        @input="fileSelected()"
        ref="upload">
      <v-btn link class="primary" v-if="$refs.upload && !$refs.upload.dropActive && (!files || files.length==0)">
        {{ $t('Выберите файл на вашем компьюере или перетащите мышкой') }}
      </v-btn>
    </file-upload>
    <v-alert class="success" v-if="files.length>0">{{files[0].progress}}</v-alert>
    <div v-if="$refs.upload && !$refs.upload.dropActive">
      <v-btn @click="cancel()">{{ $t('Отмена') }}</v-btn>
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
  files: VUFile[] = [];

  @Prop({required: true})
  uploadAction!: (file: File) => Promise<any>;

  private fileSelected() {
    if (this.files && this.files.length > 0) {
      return this.uploadAction(this.files[0].file)
          .finally(() => this.cancel());
    }
  }

  private cancel() {
    this.files.length = 0;
    this.$emit('cancel');
  }

}

</script>


<style lang="css">

.upload-area {
  position: relative;
  border: grey 2px dashed;
  border-radius: 5px;
  text-align: center;
  vertical-align: middle;
  padding-top: 50px;
  padding-bottom: 50px;
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
  background-color: rgba(0, 0, 0, 0.5);;
}

</style>
