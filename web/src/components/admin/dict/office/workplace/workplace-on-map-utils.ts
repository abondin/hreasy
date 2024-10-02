import {DictOfficeWorkplace, DictOfficeWorkplaceType} from "@/components/admin/dict/dict.admin.service";
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

    public static getOrCreateWorkplaceIcon(svg: SVGElement, workplace: DictOfficeWorkplace, clickListener?: (workplace: DictOfficeWorkplace) => any) {
        const icon = svg.querySelector(`[data-id='${workplace.id?.toString()}'`);

        if (workplace.mapX && workplace.mapY && !icon) {

            const newGroup = this.createGroupElement(workplace, clickListener);
            const newIcon = this.createIconElement(workplace);
            const newText = this.createTextElement(workplace);
            const tooltip = this.createTooltipElement(workplace);

            newGroup.appendChild(tooltip);
            newGroup.appendChild(newIcon);
            newGroup.appendChild(newText);

            svg.appendChild(newGroup);
        }
    }

    private static createGroupElement(workplace: DictOfficeWorkplace, clickListener?: (workplace: DictOfficeWorkplace) => any): SVGGElement {
        const newGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        newGroup.setAttribute('data-id', workplace.id.toString());
        newGroup.setAttribute('class', 'workplace-icon');

        if (clickListener) {
            newGroup.style.cursor = 'pointer';
            newGroup.addEventListener('click', (e: MouseEvent) => {
                logger.log(`clicked`, e);
                clickListener(workplace);
                e.stopPropagation();
            });
        }

        return newGroup;
    }

    private static createIconElement(workplace: DictOfficeWorkplace): SVGGeometryElement {
        const newIcon = document.createElementNS('http://www.w3.org/2000/svg', 'rect');

        const width = 60;
        const height = 40;

        const x = workplace.mapX! - width / 2;
        const y = workplace.mapY! - height / 2;
        const backgroundColor = this.getWorkplaceIconColor(workplace.type);

        newIcon.setAttribute('x', x.toString());
        newIcon.setAttribute('y', y.toString());
        newIcon.setAttribute('width', width.toString());
        newIcon.setAttribute('height', height.toString());
        newIcon.setAttribute('fill', backgroundColor);
        newIcon.setAttribute('data-background-color', backgroundColor);
        newIcon.setAttribute('stroke', 'black');
        newIcon.setAttribute('stroke-width', '2');

        return newIcon;
    }

    private static createTextElement(workplace: DictOfficeWorkplace): SVGTextElement {
        const newText = document.createElementNS('http://www.w3.org/2000/svg', 'text');

        newText.setAttribute('x', workplace.mapX!.toString());
        newText.setAttribute('y', workplace.mapY!.toString());
        newText.setAttribute('text-anchor', 'middle');
        newText.setAttribute('fill', 'black');
        newText.textContent = workplace.name;

        newText.style.userSelect = 'none';

        return newText;
    }

    private static createTooltipElement(workplace: DictOfficeWorkplace): SVGTitleElement {
        const tooltip = document.createElementNS('http://www.w3.org/2000/svg', 'title');
        tooltip.textContent = `${workplace.name}. ${workplace.description || ''}`;

        return tooltip;
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

    public static removeAllWorkplaces(svg: SVGElement) {
        const icons = svg.querySelectorAll('.workplace-icon');
        icons.forEach(icon => {
            icon.remove();
        });
    }

    public static getWorkplaceIcon(type: DictOfficeWorkplaceType) {
        let result = '❓';
        switch (type) {
            case DictOfficeWorkplaceType.REGULAR:
                result = '🪑'
                break;
            case DictOfficeWorkplaceType.GUEST:
                result = '🅿️';
                break;
        }
        return result;
    }

    public static getWorkplaceIconColor(type: DictOfficeWorkplaceType) {
        let result = 'lightgray';
        switch (type) {
            case DictOfficeWorkplaceType.GUEST:
                result = 'lightblue';
                break;
        }
        return result;
    }


}
