import mascotCool from '@/assets/brand/mascot-cool.png'
import mascotLaptop from '@/assets/brand/mascot-laptop.png'
import mascotThinking from '@/assets/brand/mascot-thinking.png'
import mascotThumbsUp from '@/assets/brand/mascot-thumbsup.png'
import mascotWave from '@/assets/brand/mascot-wave.png'

const POSES = {
  wave: mascotWave,
  laptop: mascotLaptop,
  cool: mascotCool,
  thinking: mascotThinking,
  thumbsUp: mascotThumbsUp,
} as const

export type MascotPose = keyof typeof POSES

export function Mascot({
  className = 'h-28 w-28',
  pose = 'wave',
}: {
  className?: string
  pose?: MascotPose
}) {
  return <img src={POSES[pose]} alt="" className={`${className} object-contain`} aria-hidden />
}
