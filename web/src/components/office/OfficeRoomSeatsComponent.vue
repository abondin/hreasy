<template>
  <v-card :loading="loading">
    <v-card-text>
      <v-alert v-if="error" type="error">
        {{ error }}
      </v-alert>
      <div class="d-flex flex-row">
        <div v-html="svgSource" ref="seatsContainer" style="width: 800px; height: 600px"></div>
        <v-card class="ml-5">
          <v-card-text>
            <p v-if="selectedSeat">
              {{ $t('Выбранное место') }}: {{ selectedSeat.name }}
              <br>
              <span v-if="selectedSeat.employee">
              {{ $t('Сотрудник') }}: {{ selectedSeat.employee.name }}
            </span>
            </p>
            <p v-else>{{ $t('Выберите место для получения информации') }}</p>
          </v-card-text>
        </v-card>
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import logger from "@/logger";
import dictService from "@/store/modules/dict.service";
import {errorUtils} from "@/components/errors";
import {nextTick} from "vue";

interface OfficeRootSeat {
  id: number,
  name: string,
  position: {
    x: number,
    y: number
  },
  employee?: {
    id: string
    name: string
  }
}

@Component({
  name: "OfficeRoomSeatsComponent",
  components: {}
})
export default class OfficeRoomSeatsComponent extends Vue {

  private officeLocationIdQueryParam = 2;

  private error: string | null = null;
  private loading = false;
  private svgSource: string | null = null;

  private selectedSeat: OfficeRootSeat | null = null;
  private svg: SVGSVGElement | null = null;

  private seats: OfficeRootSeat[] = [
    {id: 1, position: {x: 175, y: 75}, name: '1'},
    {id: 2, position: {x: 325, y: 175}, name: '2a'},
    {id: 3, position: {x: 475, y: 175}, name: '3a', employee: {id: '1', name: 'Иванов Иван Иванович'}},
  ]

  mounted() {
    return this.fetchMap();
  }

  fetchMap() {
    this.loading = true;
    this.error = null;
    return dictService.getOfficeLocationMap(this.officeLocationIdQueryParam)
        .then(data => {
          this.svgSource = data;
          return nextTick(()=>this.applyDataToMap());
        })
        .catch(e => {
          this.error = errorUtils.shortMessage(e);
        }).finally(() => {
          this.loading = false;
        })
  }

  applyDataToMap() {
    this.svg = (this.$refs.seatsContainer as HTMLDivElement).querySelector('svg') as SVGSVGElement;
    if (!this.svg) {
      this.error = this.$t('Не удалось построить план комнаты').toString();
    } else {
      this.svg.addEventListener('click', (e: MouseEvent) => {
        const target = e.target as SVGElement;
        logger.log("target", target);
        if (target.getAttribute('role') !== 'seat') {
          this.selectedSeat = null;
        }
      });
      this.seats.forEach((seat) => {
        this.addSeat(seat);
      });
    }

  }

  handleSeatClick(seat: OfficeRootSeat) {
    this.selectedSeat = seat;
  }

  private addSeat(seat: OfficeRootSeat) {
    const seatElement = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    // Group element
    const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    g.setAttribute('transform', `translate(${seat.position.x}, ${seat.position.y})`);
    g.setAttribute('role', `seat`);

    // Link to handle actions
    const link = document.createElementNS('http://www.w3.org/2000/svg', 'a');
    link.setAttribute('href', '#');
    link.setAttribute('title', seat.name);
    link.setAttribute('role', `seat`);
    link.addEventListener('click', (e) => {
      e.preventDefault();
      this.handleSeatClick(seat); // Вызов функции при клике
    });

    const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    circle.setAttribute('role', `seat`);
    circle.setAttribute('cx', '0');
    circle.setAttribute('cy', '0');
    circle.setAttribute('r', '15');
    circle.setAttribute('fill', '#4682B4');
    circle.setAttribute('stroke', '#000');
    circle.setAttribute('stroke-width', '1');

    const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    text.setAttribute('role', `seat`);
    text.setAttribute('x', '-9');
    text.setAttribute('y', '5');
    text.setAttribute('font-family', 'Arial');
    text.setAttribute('font-size', '16');
    text.setAttribute('fill', '#fff');
    text.textContent = this.getUtf8Icon(seat);

    link.appendChild(circle);
    link.appendChild(text);
    g.appendChild(link);

    // Add to svg
    this.svg!.appendChild(g);
  }

  private getUtf8Icon(seat: OfficeRootSeat) {
    return seat.employee ? '👨‍💼' : '🪑';
  }

}
</script>

