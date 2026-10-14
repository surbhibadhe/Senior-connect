import ResourceCard from './ResourceCard'
import type { ResourceResponse } from '../types'

export default function ResourceRow({ r }: { r: ResourceResponse }) {
  return <ResourceCard resource={r} />
}
