import { describe, expect, it } from 'vitest'

import { ROLE_VISUALS } from '@/features/roles/roleVisuals'

function contrastRatio(first: string, second: string) {
  const luminance = (color: string) => {
    const channels = color.slice(1).match(/.{2}/g)!.map((channel) => parseInt(channel, 16) / 255)
    const linear = channels.map((channel) => channel <= 0.04045 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4)
    return 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2]
  }
  const values = [luminance(first), luminance(second)].sort((a, b) => b - a)
  return (values[0] + 0.05) / (values[1] + 0.05)
}

describe('role visual identity', () => {
  it('maps each V2 role to its official emblem and approved palette', () => {
    const expected = {
      'backend-dev-jr': ['#E8F4FC', '#0065BE'],
      'frontend-dev-jr': ['#F5EBFC', '#6B209E'],
      'fullstack-dev-jr': ['#E5F8F8', '#007B89'],
      'qa-jr': ['#FFF3E1', '#B95408'],
      'data-analyst-jr': ['#F0F8E5', '#397A20'],
      'cloud-jr': ['#EAF0F5', '#3C5978'],
    } as const

    expect(Object.keys(ROLE_VISUALS).sort()).toEqual(Object.keys(expected).sort())
    for (const [roleId, [background, diagnosis]] of Object.entries(expected)) {
      const visual = ROLE_VISUALS[roleId as keyof typeof ROLE_VISUALS]
      expect(visual.backgroundColor).toBe(background)
      expect(visual.diagnosisColor).toBe(diagnosis)
      expect(visual.emblem).toMatch(/\.svg$/)
      expect(visual.label).toBeTruthy()
      expect(visual.emblemAlt).toContain(visual.label)
      expect(contrastRatio(visual.diagnosisColor, '#FFFFFF')).toBeGreaterThanOrEqual(4.5)
      expect(contrastRatio(visual.backgroundColor, '#163049')).toBeGreaterThanOrEqual(4.5)
    }
  })
})
