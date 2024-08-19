import {ReportPeriod} from "@/components/overtimes/overtime.service";

/**
 * TableComponentDataContainer must implements this interface to support HreasyTableSelectPeriodAction toolbar buttons
 */
export interface DataContainerWithPeriodSelectionSupport {
    get selectedPeriod(): ReportPeriod;

    get periodClosed(): boolean;

    /**
     * When data loading from the server. Needs to disable close and reopen buttons
     */
    get loading(): boolean;

    canClosePeriod(): boolean;

    incrementPeriod(): void;
    decrementPeriod(): void;

    closePeriod(): any;
    reopenPeriod(): any;
}
