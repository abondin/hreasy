import {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";
import logger from "@/logger";

export default class WorkplaceOnMapUtils {

    public static defaultMapSizes = {
        width: 800,
        height: 600
    };

    public static adjustSvgViewBox(svg: SVGElement) {
        const svgElement = svg.querySelector('svg');
        if (svgElement && !svgElement.hasAttribute('viewBox')) {
            // Original svg size
            const svgWidth = svgElement.getAttribute('width') || this.defaultMapSizes.width;
            const svgHeight = svgElement.getAttribute('height') || this.defaultMapSizes.height;

            // Adjust to default map size
            svgElement.setAttribute('viewBox', `0 0 ${svgWidth} ${svgHeight}`);
            svgElement.setAttribute('width', `${this.defaultMapSizes.width}`);
            svgElement.setAttribute('height', `${this.defaultMapSizes.height}`);
            svgElement.setAttribute('preserveAspectRatio', 'xMidYMin meet');
        }
    }

    public static initializeWorkplace(svg: SVGElement) {
        const elements = svg.querySelectorAll('*');
        elements.forEach(element => {
            if (element instanceof SVGElement && element.dataset && element.dataset['workplacename']) {
                const dataset = element.dataset;
                logger.log(`found workplace ${element.getAttribute('data-workplacename')}`, element);
                const workplaceName = dataset['workplacename'];
                const workplaceType = Number(dataset['workplacetype']);
                element.addEventListener('click', (e) => {
                    console.log(`clicked ${workplaceType}:${workplaceName}`, e);
                })
            }
        })
    }


    public static highlightWorkplace(svg: SVGElement, selectedWorkplace: DictOfficeWorkplace | null) {
        // Reset previous highlights (remove highlights from all workplaces)
        const allIcons = svg.querySelectorAll('.workplace-icon rect');
        allIcons.forEach(icon => {
            icon.setAttribute('fill', icon.getAttribute('data-background-color') || 'lightgray');
        });

        // Highlight the selected workplace
        if (selectedWorkplace) {
            const selectedIcon = svg.querySelector(`[data-id='${selectedWorkplace.id?.toString()}'] rect`);
            if (selectedIcon) {
                selectedIcon.setAttribute('fill', 'lightgreen');
            }
        }
    }


}
