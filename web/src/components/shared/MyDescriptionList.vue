<template>
  <span>
  <dl class="attrs-dotted" v-for="(item,_) in items" v-bind:key="_">
    <dt>{{ item.t }}</dt>
    <dd>{{ item.d }}</dd>
  </dl>
    </span>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";

export interface DlItem {
  /**
   * term
   */
  t: string,
  /**
   * defenition
   */
  d: string | null
}

@Component
export default class MyDescriptionList extends Vue {
  @Prop({required: true})
  private items!: DlItem[];

}
</script>

<style scoped>
.attrs-dotted {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  margin: 0 0 10px 0;
}

.attrs-dotted dt {
  width: 40%;
  margin: 0 0 8px 0;
  padding: 0;
  display: flex;
  box-sizing: border-box;
  align-items: flex-end;
  position: relative;
  word-break: break-word;
  color: #808d9a;
}

.attrs-dotted dt:before {
  content: "";
  display: block;
  width: 100%;
  position: absolute;
  left: 0;
  bottom: .2em;
  border-bottom: 1px dotted #d6dbe0;
}

.attrs-dotted dt span {
  padding-right: 5px;
  background: #fff;
  position: relative;
}

.attrs-dotted dd {
  width: 60%;
  display: flex;
  box-sizing: border-box;
  align-items: flex-end;
  margin: 0 0 8px 0;
  padding: 0 0 0 5px;
  word-break: break-word;
}
</style>
