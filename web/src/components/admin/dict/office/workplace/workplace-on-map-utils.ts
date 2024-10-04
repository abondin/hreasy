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
                element.addEventListener('mouseenter', (e) => {
                    element.style.strokeWidth = '3px';
                })
                element.addEventListener('mouseleave', (e) => {
                    element.style.strokeWidth = '1px';
                })
            }
        })
    }

    public static findWorkplaceSvgElement(svg: SVGElement, workplaceName: string): SVGElement | undefined {
        let result = undefined;
        svg.querySelectorAll('*').forEach(element => {
            if (element instanceof SVGElement && element.dataset && element.dataset['workplacename'] == workplaceName) {
                result = element;
            }
        });
        return result;
    }


    public static highlightWorkplace(svg: SVGElement, workplaceName: string | null) {
        // Highlight the selected workplace
        if (workplaceName) {
            const element = this.findWorkplaceSvgElement(svg, workplaceName);
            if (element) {
                element.setAttribute('fill', 'lightgreen');
            }
        }
    }


}
