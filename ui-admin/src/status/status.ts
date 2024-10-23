export type SystemStatus = {
    ok: boolean
    systems: Record<string, SubsystemStatus>[]
}

type SubsystemStatus = {
    ok: boolean
}
